package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.YarnJobSpark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 苍蝇系统-yarn管理-任务查询
 *
 * @author 刘蒙
 */
public interface GoshawkYarnJobSparkRepository
        extends JpaRepository<YarnJobSpark, Integer>, JpaSpecificationExecutor<YarnJobSpark> {

    @Query(value = "select queue_name from t_cluster_yarn_spark group by queue_name", nativeQuery = true)
    List<String> queue();
}
