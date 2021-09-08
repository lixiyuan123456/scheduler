package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.Threshold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 阈值
 *
 * @author 刘蒙
 */
public interface GoshawkThresHoldRepository
        extends JpaRepository<Threshold, Integer>, JpaSpecificationExecutor<Threshold> {

    @Transactional
    @Modifying
    @Query(value = "update t_cluster_threshold set `threshold_key`=?1,`threshold_name`=?2,`threshold_value`=?3  where `id`=?4 ",
            nativeQuery = true)
    Integer update(String key, String name, Integer value, Integer id);
}
