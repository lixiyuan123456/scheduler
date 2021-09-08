package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.HadoopBindingRepository;
import com.naixue.nxdp.model.HadoopBinding;

import lombok.Data;

@Service
public class HadoopBindingService {

    @Autowired
    HadoopBindingRepository hadoopBindingRepository;

    public String getHadoopUserGroupName(Integer deptId) {
        HadoopBinding binding = findByDeptId(deptId);
        if (binding == null) {
            throw new RuntimeException("部门id=" + deptId + "对应的hadoop用户组不存在");
        }
        return binding.getProxyCode();
    }

    public List<HadoopBinding> getAllHadoopBindings() {
        return hadoopBindingRepository.findAll();
    }

    public List<HadoopUserGroup> getAllHadoopUserGroups() {
        List<HadoopBinding> hadoopBindings = getAllHadoopBindings();
        List<HadoopUserGroup> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(hadoopBindings)) {
            Map<String, HadoopUserGroup> data = new HashMap<>();
            for (HadoopBinding hadoopBinding : hadoopBindings) {
                HadoopUserGroup hadoopUserGroup = data.get(hadoopBinding.getProxyCode());
                if (hadoopUserGroup == null) {
                    hadoopUserGroup = new HadoopUserGroup();
                    hadoopUserGroup.setName(hadoopBinding.getProxyCode());
                    data.put(hadoopBinding.getProxyCode(), hadoopUserGroup);
                }
                hadoopUserGroup.getDeptIds().add(hadoopBinding.getDeptId());
            }
            result = new ArrayList<>(data.values());
        }
        return result;
    }

    public HadoopBinding findByDeptId(Integer deptId) {
        return hadoopBindingRepository.findByDeptId(deptId);
    }

    public boolean exist(Integer deptId) {
        return findByDeptId(deptId) == null ? false : true;
    }

    @Transactional
    public void createHadoopBindings(Integer deptId, String hadoopUserGroupName) {
        Assert.notNull(deptId, "请求参数deptId不允许为空");
        Assert.hasText(hadoopUserGroupName, "请求参数hadoopUserGroupName不允许为空");
        // 校验是否已经存在
        if (exist(deptId)) {
            throw new RuntimeException("该部门的hadoop用户组已经存在不允许再次创建");
        }
        HadoopBinding hadoopBinding = new HadoopBinding();
        hadoopBinding.setDeptId(deptId);
        hadoopBinding.setProxyCode(hadoopUserGroupName);
        hadoopBinding.setHadoopCode(hadoopUserGroupName); // 暂时设置成一样
        hadoopBindingRepository.save(hadoopBinding);
    }

    @Data
    public static class HadoopUserGroup {

        private String name;

        private Set<Integer> deptIds = new HashSet<>();
    }
}
