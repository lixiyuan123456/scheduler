package com.naixue.nxdp.platform.service.zstream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Strings;
import com.naixue.zzdp.common.util.ShellUtils;
import com.naixue.nxdp.data.dao.zstream.ZstreamJobDao;
import com.naixue.nxdp.data.model.zstream.ZstreamJob;
import com.naixue.nxdp.data.model.zstream.ZstreamJobLog;
import com.naixue.nxdp.data.model.zstream.ZstreamUdx;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ZstreamJobService {

  @Autowired private ZstreamJobDao zstreamJobDao;

  @Autowired private ZstreamUdxService zstreamUdxService;

  @Autowired private ZstreamJobLogService zstreamJobLogService;

  @Autowired private Executor taskExecutor;

  @Transactional
  public ZstreamJob save(ZstreamJob job) {
    return save(job, null);
  }

  @Transactional
  public ZstreamJob save(ZstreamJob job, List<Integer> udxIds) {
    if (job.getId() != null && job.getJobConfig() != null) {
      job.getJobConfig().setId(job.getId());
    }
    if (!CollectionUtils.isEmpty(udxIds)) {
      List<ZstreamUdx> udxs = new ArrayList<>();
      for (Integer udxId : udxIds) {
        ZstreamUdx udx = zstreamUdxService.findUdxById(udxId);
        udxs.add(udx);
      }
      job.setUdxs(udxs);
    }
    // 更新操作
    if (job.getId() != null) {
      ZstreamJob dbJob = zstreamJobDao.findOne(job.getId());
      job.setStatus(dbJob.getStatus());
      job.setLastJobVersion(dbJob.getLastJobVersion());
      job.setLastApplicationId(dbJob.getLastApplicationId());
      job.setLastApplicationJobId(dbJob.getLastApplicationJobId());
    }
    return zstreamJobDao.save(job);
  }

  public ZstreamJob findJobById(Integer jobId) {
    return findJobById(jobId, true);
  }

  public ZstreamJob findJobById(Integer jobId, boolean lazy) {
    ZstreamJob job = zstreamJobDao.findOne(jobId);
    if (!lazy) {
      job.getUdxs().size();
    }
    return job;
  }

  @Transactional
  public void deleteJobById(final Integer jobId) {
    ZstreamJob job = zstreamJobDao.findOne(jobId);
    job.setStatus(ZstreamJob.Status.DELETED.getCode());
    zstreamJobDao.save(job);
    zstreamJobLogService.deleteByJobId(jobId);
  }

  @Transactional
  public void killJob(final ZstreamJob job) {
    if (StringUtils.isEmpty(job.getLastApplicationJobId())
        || StringUtils.isEmpty(job.getLastApplicationId())) {
      throw new RuntimeException("application id or application job id is not exists");
    }
    ShellUtils.exec(
        "flink cancel " + job.getLastApplicationJobId() + " -yid " + job.getLastApplicationId());
    job.setStatus(ZstreamJob.Status.KILLED.getCode());
    // job.setLastApplicationId("");
    // job.setLastApplicationJobId("");
    zstreamJobDao.save(job);
    zstreamJobLogService.updateLog(
        job.getId(), job.getLastJobVersion(), ZstreamJob.Status.getStatus(job.getStatus()));
  }

  @Transactional
  public void doLaunchJob(final ZstreamJob job, boolean resume) {
    String command =
        " Flink-SQL-Core.sh "
            + job.getId()
            + " submit "
            + (resume ? " savepoint " : " nosavepoint ")
            + job.getProxyCode();
    String runLog = ShellUtils.exec(command);
    JobSubmitResult result = parseShellResponse(runLog);
    log.debug("解析Zstream日志：", result.toString());
    job.setLastApplicationId(result.getApplicationId());
    job.setLastApplicationJobId(result.getApplicationJobId());
    if (result.getCode().equals("1")) { // 资源不足
      job.setStatus(ZstreamJob.Status.BOOT_FAILURE.getCode());
    } else if (result.getCode().equals("2")) { // 启动失败
      job.setStatus(ZstreamJob.Status.BOOT_FAILURE.getCode());
    } else if (result.getCode().equals("3")) { // 启动成功
      job.setStatus(ZstreamJob.Status.RUNNING.getCode());
    } else {
      throw new RuntimeException("解析日志失败：" + result.toString());
    }
    ZstreamJob newJob = zstreamJobDao.save(job);
    ZstreamJobLog newJobLog =
        zstreamJobLogService.insertLog(
            newJob.getId(),
            result.getApplicationId(),
            result.getApplicationJobId(),
            runLog,
            ZstreamJob.Status.getStatus(newJob.getStatus()));
    newJob.setLastJobVersion(newJobLog.getJobVersion());
    zstreamJobDao.save(newJob);
    // return result;
  }

  @Transactional
  public void launchJob(final ZstreamJob job, boolean resume) {
    if (resume && ZstreamJob.Status.NEW.getCode().equals(job.getStatus())) {
      throw new RuntimeException("任务不可以从上次停止的地方恢复任务，因为当前任务的状态为NEW");
    }
    // 修改任务的状态为等待信号
    job.setStatus(ZstreamJob.Status.LAUNCHING.getCode());
    zstreamJobDao.save(job);
    taskExecutor.execute(
        new Runnable() {

          @Override
          public void run() {
            doLaunchJob(job, resume);
          }
        });
  }

  @Deprecated
  public JobSubmitResult checkJobSqlGrammarByJobId(final Integer jobId) {
    ZstreamJob job = findJobById(jobId);
    Assert.notNull(job, "jobId=" + jobId + "任务不存在");
    String command =
        " Flink-SQL-Core.sh " + job.getId() + " check nosavepoint " + job.getProxyCode();
    String runLog = ShellUtils.exec(command);
    return parseShellResponse(runLog);
  }

  public JobSubmitResult checkJobSqlGrammar(final String sql, final String udxs) {
    Assert.hasText(sql, "SQL不允许为空");
    Assert.hasText(udxs, "UDXs不允许为空");
    StringBuilder command = new StringBuilder();
    command.append(" python $FLINK_HOME/bin/CheckSQL.py ");
    command.append(" -sql " + " \" " + sql + " \" ");
    command.append(" -udxs " + udxs);
    String runLog = ShellUtils.exec(command.toString());
    return parseShellResponse(runLog);
  }

  public Page<ZstreamJob> listJobs(ZstreamJob condition, Integer pageIndex, Integer pageSize) {
    return listJobs(condition, pageIndex, pageSize, new Sort.Order(Sort.Direction.DESC, "id"));
  }

  public Page<ZstreamJob> listJobs(
      ZstreamJob condition, Integer pageIndex, Integer pageSize, Sort.Order... orders) {
    Specification<ZstreamJob> specification =
        new Specification<ZstreamJob>() {

          @Override
          public Predicate toPredicate(
              Root<ZstreamJob> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            if (Objects.isNull(condition)) {
              return null;
            }
            List<Predicate> data = new ArrayList<>();
            if (!Strings.isNullOrEmpty(condition.getProxyCode())
                && !condition.getProxyCode().equals("0")) {
              data.add(cb.equal(root.get("proxyCode"), condition.getProxyCode()));
            }
            if (!Strings.isNullOrEmpty(condition.getCreator())
                && !condition.getCreator().equals("0")) {
              data.add(cb.equal(root.get("creator"), condition.getCreator()));
            }
            if (!Strings.isNullOrEmpty(condition.getNameLike())) {
              data.add(cb.like(root.get("name"), "%" + condition.getNameLike() + "%"));
            }
            data.add(cb.notEqual(root.get("status"), ZstreamJob.Status.DELETED.getCode()));
            return cb.and(data.toArray(new Predicate[data.size()]));
          }
        };
    Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(orders));
    return zstreamJobDao.findAll(specification, pageable);
  }

  private JobSubmitResult parseShellResponse(String resp) {
    final Matcher codeMatcher = Pattern.compile("#{6}\\[\\d\\]#{6}").matcher(resp);
    String code =
        codeMatcher.find()
            ? codeMatcher.group().replace("######", "").replace("[", "").replace("]", "")
            : "";
    final Matcher applicationIdMatcher = Pattern.compile("application_\\d+_\\d+").matcher(resp);
    String applicationId = applicationIdMatcher.find() ? applicationIdMatcher.group() : "";
    final Matcher applicationJobIdMatcher =
        Pattern.compile("application_job_id_\\w+").matcher(resp);
    String applicationJobId =
        applicationJobIdMatcher.find()
            ? applicationJobIdMatcher.group().replace("application_job_id_", "")
            : "";
    // final Matcher exceptionMatcher = Pattern.compile("Caused by:.*(\\r|\\n)").matcher(resp);
    // String message = exceptionMatcher.find() ? exceptionMatcher.group() : "";
    return new JobSubmitResult(code, applicationId, applicationJobId, resp);
  }

  @ToString
  @Getter
  @Setter
  @AllArgsConstructor
  public static class JobSubmitResult {

    private String code = "";

    private String applicationId = "";

    private String applicationJobId = "";

    private String message = "";

    public String getMessage() {
      if (StringUtils.isEmpty(this.message)) {
        if (this.code.equals("1")) {
          this.message = "资源不足";
        } else if (this.code.equals("2")) {
          this.message = "启动失败";
        } else if (this.code.equals("3")) {
          this.message = "启动成功";
        }
      }
      return this.message;
    }
  }
}
