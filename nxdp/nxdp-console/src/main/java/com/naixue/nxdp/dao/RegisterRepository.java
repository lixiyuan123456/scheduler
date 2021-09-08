package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.Register;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegisterRepository extends JpaRepository<Register, Integer> {

    Register findTop1ByUserIdOrderByCreateTimeDesc(String userId);

    List<Register> findAllByUserId(String userId);
}
