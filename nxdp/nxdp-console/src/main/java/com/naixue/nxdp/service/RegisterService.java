package com.naixue.nxdp.service;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.attachment.hue.service.HueService;
import com.naixue.nxdp.dao.AccountRepository;
import com.naixue.nxdp.dao.RegisterRepository;
import com.naixue.nxdp.dao.UserRepository;
import com.naixue.nxdp.model.Account;
import com.naixue.nxdp.model.HadoopBinding;
import com.naixue.nxdp.model.Register;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.User.PermissionLevel;
import com.naixue.nxdp.thirdparty.WXHelper;

import lombok.Data;

@Service
public class RegisterService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    // @Autowired private UserDepartmentRepository userDepartmentRepository;

    @Autowired
    private RegisterRepository registerRepository;

    @Autowired
    private HueService hueService;

    @Autowired
    private HadoopBindingService hadoopBindingService;

    @Transactional
    public void register(Register register) {
        Assert.notNull(register, "请求参数为空");
        Register exist = registerRepository.findTop1ByUserIdOrderByCreateTimeDesc(register.getUserId());
        if (exist != null && Register.Status.WAIT.getCode().equals(exist.getStatus())) {
            throw new RuntimeException("申请过，请等待审核");
        }
        Account account = accountRepository.findOne(register.getUserId());
        Assert.notNull(account, "账户不存在ID=" + register.getUserId());
        // UserDepartment userDepartment = userDepartmentRepository.findOne(register.getDeptId());
        // Assert.notNull(userDepartment, "部门数据不存在");
        register.setDeptId(account.getDeptId());
        register.setDeptName(account.getDeptName());
        registerRepository.save(register);
        List<User> admins = userRepository.findByPermissionLevel(User.PermissionLevel.ADMIN.getCode());
        if (!CollectionUtils.isEmpty(admins)) {
            for (User admin : admins) {
                WXHelper.asyncSendWXMsg(
                        "用户" + register.getUserPyname() + "正在申请注册zzdp平台，请前往zzdp审批。", admin.getPyName());
            }
        }
        WXHelper.asyncSendWXMsg("您正在申请注册zzdp平台，已经通知管理员，请耐心等待。", register.getUserName());
    }

    public List<Register> listRegistersByUserId(String userId) {
        Assert.hasText(userId, "请求参数userId不允许为空");
        return registerRepository.findAllByUserId(userId);
    }

    public Page<Register> listRegisters(Integer pageIndex, Integer pageSize) {
        return registerRepository.findAll(
                new PageRequest(pageIndex, pageSize, new Sort(Direction.DESC, "id")));
    }

    @Transactional
    public void operate(
            User currentUser, Integer registerId, Integer operate, String hadoopUserGroupName) {
        Assert.notNull(registerId, "请求参数registerId不允许为空");
        Operate op = Operate.getEnum(operate);
        Register register = registerRepository.findOne(registerId);
        if (register == null) {
            return;
        }
        if (Operate.YES == op) {
            User u = userRepository.findOne(register.getUserId());
            if (u == null) {
                Account account = accountRepository.findOne(register.getUserId());
                u = new User();
                u.setId(account.getId());
                u.setPyName(account.getPyname());
                u.setName(account.getName());
                u.setEmail(account.getEmail());
                u.setDeptId(account.getDeptId());
                u.setDeptName(account.getDeptName());
                userRepository.save(u);
            }
            register.setStatus(Register.Status.RATIFY.getCode());
            // 异步创建HUE账户
            HadoopBinding hadoopBinding = hadoopBindingService.findByDeptId(u.getDeptId());
            if (hadoopBinding == null) {
                hadoopBinding = new HadoopBinding();
                hadoopBinding.setDeptId(u.getDeptId());
                hadoopBinding.setProxyCode(hadoopUserGroupName);
            } else {
                if (!hadoopBinding.getProxyCode().equals(hadoopUserGroupName)) {
                    throw new RuntimeException("新用户的部门已经存在指定的hadoop用户组了，不允许重新指定");
                }
            }
            u.setHadoopBinding(hadoopBinding);
            new Thread(new CreateHueAccount(hueService, u)).start();
        }
        if (Operate.NO == op) {
            register.setStatus(Register.Status.REJECT.getCode());
        }
        register.setOperatorId(currentUser.getId());
        register.setOperatorName(currentUser.getName());
        register.setModifyTime(new Date());
        registerRepository.saveAndFlush(register);
        // 异步通知
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        String toUser = "管理员已经" + op.getName() + "您的请求，请悉知。";
                        WXHelper.asyncSendWXMsg(toUser, register.getUserPyname());
                        List<User> admins =
                                userRepository.findByPermissionLevel(PermissionLevel.ADMIN.getCode());
                        if (CollectionUtils.isEmpty(admins)) {
                            return;
                        }
                        String[] adminNames = new String[admins.size()];
                        for (int i = 0; i < admins.size(); i++) {
                            adminNames[i] = admins.get(i).getPyName();
                        }
                        String toAdmin =
                                "管理员"
                                        + currentUser.getName()
                                        + "("
                                        + currentUser.getPyName()
                                        + ")"
                                        + op.getName()
                                        + ""
                                        + register.getUserPyname()
                                        + "的请求，请悉知。";
                        WXHelper.asyncSendWXMsg(toAdmin, adminNames);
                    }
                })
                .start();
    }

    private static enum Operate {
        NO(0, "拒绝"),

        YES(1, "同意");

        private Integer code;
        private String name;

        Operate(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        public static Operate getEnum(Integer code) {
            for (Operate e : Operate.values()) {
                if (e.getCode().equals(code)) {
                    return e;
                }
            }
            throw new IllegalArgumentException("没有对应的枚举值");
        }

        public Integer getCode() {
            return code;
        }

        public String getName() {
            return name;
        }
    }

    @Data
    private static class CreateHueAccount implements Runnable {

        private HueService hueService;

        private User user;

        CreateHueAccount(HueService hueService, User user) {
            this.hueService = hueService;
            this.user = user;
        }

        @Override
        public void run() {
            hueService.createOrModifyDefaultHueAccount(user);
        }
    }
}
