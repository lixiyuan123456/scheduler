package com.naixue.nxdp.attachment.goshawk.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.naixue.nxdp.attachment.goshawk.dao.HiveTableAccessApplicationRepository;
import com.naixue.nxdp.attachment.goshawk.model.HiveTableAccessApplication;
import com.naixue.zzdp.common.util.ShellUtils;

@Service
public class HiveTableAccessApplicationService {

    @Autowired
    private HiveTableAccessApplicationRepository hiveTableAccessApplicationRepository;

    @Autowired
    private UserService userService;

    public HiveTableAccessApplication applyHiveTableAccessApplication(
            HiveTableAccessApplication application, User currentUser) {
        validateRepeatApplication(
                application.getDbId(), application.getTableId(), application.getProxyAccount());
        User u = userService.getUserByUserId(currentUser.getId());
        application.setCreator(currentUser.getPyName());
        application.setCreatorName(currentUser.getName());
        // application.setCreatorDeptName(u.getDepartment() == null ? "" : u.getDepartment().getName());
        application.setCreatorDeptName(u.getDeptName());
        application.setCreateTime(new Date());
        application.setModifyTime(new Date());
        return hiveTableAccessApplicationRepository.save(application);
    }

    public Page<HiveTableAccessApplication> listHiveTableAccessApplications(
            HiveTableAccessApplication queryCondition, Integer pageIndex, Integer pageSize) {
        return hiveTableAccessApplicationRepository.findAll(
                new Specification<HiveTableAccessApplication>() {

                    @Override
                    public Predicate toPredicate(
                            Root<HiveTableAccessApplication> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        List<Predicate> conditions = new ArrayList<>();
                        if (queryCondition != null) {
                            if (!StringUtils.isEmpty(queryCondition.getCreator())) {
                                conditions.add(cb.equal(root.get("creator"), queryCondition.getCreator()));
                            }
                        }
                        return cb.and(conditions.toArray(new Predicate[conditions.size()]));
                    }
                },
                new PageRequest(pageIndex, pageSize, new Sort(new Order(Direction.DESC, "id"))));
    }

    @Transactional
    public void assessHiveTableAccessApplication(
            Integer id, String assessComment, HiveTableAccessApplication.Status status) {
        Assert.notNull(id, "入参id不允许为空");
        HiveTableAccessApplication application = hiveTableAccessApplicationRepository.findOne(id);
        if (HiveTableAccessApplication.Status.PASS == status) {
            validateRepeatApplication(
                    application.getDbId(), application.getTableId(), application.getProxyAccount());
            grantHiveTable(application);
        }
        application.setStatus(status);
        application.setAssessComment(assessComment == null ? "" : assessComment);
        hiveTableAccessApplicationRepository.save(application);
    }

    private void validateRepeatApplication(Integer dbId, Integer tableId, String proxyAccount) {
        List<HiveTableAccessApplication> applications =
                hiveTableAccessApplicationRepository.findByDbIdAndTableIdAndProxyAccountAndStatus(
                        dbId, tableId, proxyAccount, HiveTableAccessApplication.Status.PASS);
        if (!CollectionUtils.isEmpty(applications)) {
            throw new RuntimeException("代理用户=" + proxyAccount + "已有对该表的使用权限，不允许再次申请");
        }
    }

    private void grantHiveTable(HiveTableAccessApplication application) {
        String opKey =
                HiveTableAccessApplication.Permission.RW == application.getPermission()
                        ? " all "
                        : " select ";
        String tableName = " " + application.getDbName() + "." + application.getTableName() + " ";
        String userName = " " + application.getProxyAccount() + " ";
    /*ShellUtils.exec(
    "export",
    "HADOOP_USER_NAME=zdp",
    "&&",
    "hive",
    "-e",
    "grant" + opKey + "on table" + tableName + "to user" + userName);*/
        String grantCommand = "grant" + opKey + "on table" + tableName + "to user" + userName;
        ShellUtils.exec("export HADOOP_USER_NAME=zdp && hive -e '" + grantCommand + "'");
        String locationOpKey =
                HiveTableAccessApplication.Permission.RW == application.getPermission() ? "go=rwx" : "go+r";
        // ShellUtils.exec("hadoop", "dfs", "-chmod", "-R", locationOpKey,
        // application.getTableLocation());
        ShellUtils.exec("hadoop dfs -chmod -R " + locationOpKey + " " + application.getTableLocation());
    }
}
