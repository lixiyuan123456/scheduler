package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.AccountRepository;
import com.naixue.nxdp.dao.HadoopBindingRepository;
import com.naixue.nxdp.dao.QueueRepository;
import com.naixue.nxdp.dao.UserRepository;
import com.naixue.nxdp.model.Account;
import com.naixue.nxdp.model.HadoopBinding;
import com.naixue.nxdp.model.JobConfig.JobType;
import com.naixue.nxdp.model.Queue;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.thirdparty.WXHelper;

@Service
public class UserService {

    private static final ConcurrentHashMap<String, User> userContainer = new ConcurrentHashMap<>(50);
    @Autowired
    HadoopBindingRepository hadoopBindingRepository;

    // @Autowired private UserDepartmentRepository userDepartmentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private QueueRepository queueRepository;

    public User getUserByUserId(String userId) {
        return getUserByUserId(userId, true);
    }

    public User getUserByUserId(String userId, boolean fromCache) {
        if (fromCache && userContainer.containsKey(userId)) {
            return userContainer.get(userId);
        }
        User user = userRepository.findOne(userId);
        if (user == null) {
            return null;
        }
        // 此代码用于同步用户部门信息
        Account account = accountRepository.findOne(userId);
        if (account != null && !account.getDeptId().equals(user.getDeptId())) {
            user.setDeptId(account.getDeptId());
            user.setDeptName(account.getDeptName());
            userRepository.save(user);
        }
        // 判断部门名称是否为空
        HadoopBinding hadoopBinding = hadoopBindingRepository.findOne(user.getDeptId());
        user.setHadoopBinding(hadoopBinding);
        userContainer.putIfAbsent(userId, user);
        return user;
    }

    public User getUserByUserPyName(final String pyName) {
        return userRepository.findByPyName(pyName);
    }

    public User getUserByUserName(final String name) {
        return userRepository.findByName(name);
    }

    @Transactional
    public void deleteUserByUserId(String userId) {
        User targetUser = userRepository.findOne(userId);
        if (User.PermissionLevel.ADMIN.getCode() == targetUser.getPermissionLevel()) {
            throw new RuntimeException("非法操作：不能够删除管理员");
        }
        userRepository.delete(targetUser.getId());
        userContainer.remove(userId);
        WXHelper.asyncSendWXMsg("管理员已经将您的使用资格移除，如有疑问请联系数据平台部", targetUser.getPyName());
    }

    public Queue getBindingQueue(User user, JobType jobType) {
        Assert.notNull(user, "请求参数User不允许为空");
        Assert.notNull(jobType, "请求参数EnumJobType不允许为空");
    /*UserDepartment department = userDepartmentRepository.findByPath(user.getDepartmentpath());
    if (department == null) {
      throw new RuntimeException("根据userName=" + user.getTrueName() + "查询不到任何UserDepartment数据");
    }*/
        StringBuilder queueNameSb = new StringBuilder("root.");
        if (jobType == JobType.SPARK || jobType == JobType.SPARK_SQL) {
            queueNameSb.append("spark.");
        } else if (jobType == JobType.SPARK_STREAMING) {
            queueNameSb.append("online.");
        } else {
            queueNameSb.append("offline.");
        }
        HadoopBinding hadoopBinding = hadoopBindingRepository.findOne(user.getDeptId());
        if (hadoopBinding == null) {
            throw new NullPointerException("t_hadoop_user表未配置对应的绑定信息");
        }
        queueNameSb.append(hadoopBinding.getHadoopCode());
        return queueRepository.findByName(queueNameSb.toString());
    }

    public Map<String, User> findAllInMap() {
        List<User> users = userRepository.findAll();
        if (CollectionUtils.isEmpty(users)) {
            return null;
        }
        Map<String, User> userMap = new HashMap<>();
        for (User user : users) {
            userMap.put(user.getId(), user);
        }
        return userMap;
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public Page<User> listUsers(Integer pageIndex, Integer pageSize) {
        return userRepository.findAll(new PageRequest(pageIndex, pageSize));
    /*List<User> list = page.getContent();
    if (!CollectionUtils.isEmpty(list)) {
      List<Integer> deptIds = userRepository.distinctAllDeptIds();
      List<UserDepartment> depts = userDepartmentRepository.findByIdIn(deptIds);
      Map<Integer, UserDepartment> deptsMap = mappingWithDeptIdAndDept(depts);
      for (User u : list) {
        u.setDeptName(deptsMap.get(u.getDeptId()).getName());
      }
    }*/
        // return page;
    }

  /*public Map<Integer, UserDepartment> mappingWithDeptIdAndDept(List<UserDepartment> depts) {
    Map<Integer, UserDepartment> result = new HashMap<>();
    if (CollectionUtils.isEmpty(depts)) {
      return result;
    }
    for (UserDepartment dept : depts) {
      result.put(dept.getId(), dept);
    }
    return result;
  }*/

    public void notifyAdmins(String message) {
        List<User> admins = userRepository.findByPermissionLevel(User.PermissionLevel.ADMIN.getCode());
        if (CollectionUtils.isEmpty(admins)) {
            return;
        }
        List<String> names = new ArrayList<>();
        for (User admin : admins) {
            names.add(admin.getPyName());
        }
        WXHelper.asyncSendWXMsg(message, names.toArray(new String[names.size()]));
    }
}
