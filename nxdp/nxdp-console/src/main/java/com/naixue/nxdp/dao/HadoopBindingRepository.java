package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.HadoopBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HadoopBindingRepository extends JpaRepository<HadoopBinding, Integer> {

    HadoopBinding findByDeptId(Integer deptId);
}
