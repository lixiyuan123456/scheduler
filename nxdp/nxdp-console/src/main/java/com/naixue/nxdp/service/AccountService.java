package com.naixue.nxdp.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.dao.AccountRepository;
import com.naixue.nxdp.model.Account;
import com.naixue.nxdp.util.HttpUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    // @Autowired private UserDepartmentRepository userDepartmentRepository;

    // @Autowired private UserRepository userRepository;

    @Transactional
    public void syncAccounts() {
        log.debug("********************同步用户数据开始********************");
        String deptsJsonArr = HttpUtils.httpGet(CFG.ZZ_URL_GET_ALL_DEPTS);
        JSONArray depts = JSON.parseArray(deptsJsonArr);
        if (CollectionUtils.isEmpty(depts)) {
            return;
        }
        // 清空账户表
        accountRepository.deleteAll();
        /*
         * UserDepartment jszxUserDepartment = userDepartmentRepository.findByName("技术中心");
         * UserDepartment sstjbUserDepartment = userDepartmentRepository.findByName("搜索推荐部");
         * UserDepartment sjptbUserDepartment = userDepartmentRepository.findByName("数据平台部");
         * List<UserDepartment> userDepartments = userDepartmentRepository.findAll();
         * if (userDepartments.size() > 0) {
         * userRepository.deleteAll();
         * }
         */
        // List<UserDepartment> allDepts = userDepartmentRepository.findAll();
        Iterator<Object> iterator = depts.iterator();
        while (iterator.hasNext()) {
            JSONObject dept = (JSONObject) iterator.next();
            String url = MessageFormat.format(CFG.ZZ_URL_GET_ACCOUNTS_FROM_ONEDEPT, dept.getString("id"));
            log.debug("查询部门[{}]下的用户，url:{}", dept.getInteger("id"), url);
            String accountsJsonArr = HttpUtils.httpGet(url);
            accountsJsonArr = accountsJsonArr.replaceAll("\r|\n", "");
            if (!StringUtils.isEmpty(accountsJsonArr) && !accountsJsonArr.equals("null")) {
                JSONArray accountsArr = JSON.parseArray(accountsJsonArr);
                List<Account> accounts = new ArrayList<>();
                for (int i = 0; i < accountsArr.size(); i++) {
                    JSONObject jsonObject = accountsArr.getJSONObject(i);
                    Account account = new Account();
                    account.setId(jsonObject.getString("userid"));
                    account.setPyname(jsonObject.getString("name"));
                    account.setName(jsonObject.getString("realname"));
                    account.setEmail(jsonObject.getString("email"));
                    account.setDeptId(jsonObject.getIntValue("departmentid"));
                    account.setDeptName(dept.getString("name"));
                    accounts.add(account);
                }
                accountRepository.save(accounts);
            }
        }
        log.debug("********************同步用户数据结束********************");
    }
}
