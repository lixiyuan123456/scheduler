package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.YarnJobMapReduce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 苍蝇系统-yarn管理-任务查询
 *
 * @author 刘蒙
 */
public interface GoshawkYarnJobMapReduceRepository
        extends JpaRepository<YarnJobMapReduce, Integer>, JpaSpecificationExecutor<YarnJobMapReduce> {

    @Query(value = "select queue_name from t_cluster_yarn_mapreduce group by queue_name", nativeQuery = true)
    List<String> queue();
}
