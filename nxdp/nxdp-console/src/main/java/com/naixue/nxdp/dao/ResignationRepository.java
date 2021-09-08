package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.Resignation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ResignationRepository
        extends JpaRepository<Resignation, Integer>, JpaSpecificationExecutor<Resignation> {
}
