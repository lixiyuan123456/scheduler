package com.naixue.nxdp.platform.service.zstream;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.data.dao.zstream.ZstreamJobLogDao;
import com.naixue.nxdp.data.model.zstream.ZstreamJob;
import com.naixue.nxdp.data.model.zstream.ZstreamJobLog;

@Service
public class ZstreamJobLogService {

  @Autowired private ZstreamJobLogDao zstreamJobLogDao;

  @Transactional
  public ZstreamJobLog insertLog(final Integer jobId, final ZstreamJob.Status status) {
    return insertLog(jobId, UUID.randomUUID().toString(), "", "", "", status);
  }

  @Transactional
  public ZstreamJobLog insertLog(
      final Integer jobId, final String log, final ZstreamJob.Status status) {
    return insertLog(jobId, UUID.randomUUID().toString(), "", "", log, status);
  }

  @Transactional
  public ZstreamJobLog insertLog(
      final Integer jobId,
      final String applicationId,
      final String applicationJobId,
      final String log,
      final ZstreamJob.Status status) {
    return insertLog(
        jobId, UUID.randomUUID().toString(), applicationId, applicationJobId, log, status);
  }

  @Transactional
  public ZstreamJobLog insertLog(
      final Integer jobId,
      final String jobVersion,
      final String applicationId,
      final String applicationJobId,
      final String log,
      final ZstreamJob.Status status) {
    ZstreamJobLog jobLog =
        new ZstreamJobLog(
            jobId, jobVersion, applicationId, applicationJobId, log, status.getCode());
    return zstreamJobLogDao.save(jobLog);
  }

  public ZstreamJobLog readLog(final Integer id) {
    return zstreamJobLogDao.findOne(id);
  }

  public ZstreamJobLog readLog(final Integer jobId, final String jobVersion) {
    return zstreamJobLogDao.findByJobIdAndJobVersion(jobId, jobVersion);
  }

  public List<ZstreamJobLog> listLogs(final Integer jobId) {
    return zstreamJobLogDao.findByJobIdOrderByIdDesc(jobId);
  }

  public void updateLog(
      final Integer jobId, final String jobVersion, final ZstreamJob.Status status) {
    ZstreamJobLog log = readLog(jobId, jobVersion);
    log.setStatus(status.getCode());
    zstreamJobLogDao.save(log);
  }

  public void deleteByJobId(final Integer jobId) {
    List<ZstreamJobLog> logs = zstreamJobLogDao.findByJobIdOrderByIdDesc(jobId);
    if (CollectionUtils.isEmpty(logs)) {
      return;
    }
    for (ZstreamJobLog log : logs) {
      log.setStatus(ZstreamJob.Status.DELETED.getCode());
    }
    zstreamJobLogDao.save(logs);
  }
}
