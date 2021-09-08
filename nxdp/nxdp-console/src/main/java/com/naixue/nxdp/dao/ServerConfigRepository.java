package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.ServerConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServerConfigRepository extends JpaRepository<ServerConfig, Integer> {

    List<ServerConfig> findByServerTypeAndLogicType(Integer serverType, Integer logicType);

    ServerConfig findByName(String name);
}
