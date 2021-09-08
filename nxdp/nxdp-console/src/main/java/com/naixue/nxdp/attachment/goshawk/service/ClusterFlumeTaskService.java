package com.naixue.nxdp.attachment.goshawk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

import com.naixue.nxdp.attachment.goshawk.dao.ClusterFlumeTaskRepository;
import com.naixue.nxdp.attachment.goshawk.model.ClusterFlumeTask;

@Service
public class ClusterFlumeTaskService {

    @Autowired
    private ClusterFlumeTaskRepository clusterFlumeTaskRepository;

    public ClusterFlumeTask findOne(Integer id) {
        return clusterFlumeTaskRepository.findOne(id);
    }

    public ClusterFlumeTask saveOrUpdate(ClusterFlumeTask task) {
        return clusterFlumeTaskRepository.save(task);
    }

    public Page<ClusterFlumeTask> list(Integer pageIndex, Integer pageSize) {
        return clusterFlumeTaskRepository.findAll(
                new PageRequest(pageIndex, pageSize, new Sort(new Order(Direction.DESC, "id"))));
    }

    public void delete(Integer id) {
        clusterFlumeTaskRepository.delete(id);
    }
}
