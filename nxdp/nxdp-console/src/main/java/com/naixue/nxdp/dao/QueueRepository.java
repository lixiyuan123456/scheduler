package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.Queue;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: wangyu
 * @Created by 2018/1/30
 **/
public interface QueueRepository extends JpaRepository<Queue, Integer> {

    Queue findByName(String name);
}
