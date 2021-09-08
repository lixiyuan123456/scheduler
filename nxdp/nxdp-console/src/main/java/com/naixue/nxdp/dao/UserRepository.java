package com.naixue.nxdp.dao;

import java.util.Date;
import java.util.List;

import com.naixue.nxdp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

/**
 * @Author: wangyu @Created by 2017/11/19
 */
@Service
public interface UserRepository extends JpaRepository<User, String> {

    List<User> findByDeptId(int deptId);

    List<User> findByCreateTimeBetween(Date start, Date end);

    List<User> findByPermissionLevel(Integer permissionLevel);

    User findByPyName(final String pyName);

    User findByName(final String name);
}
