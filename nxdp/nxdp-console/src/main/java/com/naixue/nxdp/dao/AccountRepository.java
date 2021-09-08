package com.naixue.nxdp.dao;

import java.util.Collection;
import java.util.List;

import com.naixue.nxdp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AccountRepository extends JpaRepository<Account, String> {

    // Integer countByDeptId(Integer deptId);

    List<Account> findByDeptId(Integer deptId);

    List<Account> findByDeptIdIn(Collection<Integer> deptIds);

    @Override
    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE t_account", nativeQuery = true)
    void deleteAll();
}
