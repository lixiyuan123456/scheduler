package com.naixue.nxdp.dao;

import com.naixue.nxdp.model.NoticeReadLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author: wangyu @Created by 2018/1/29
 */
public interface NoticeReadLogRepository extends JpaRepository<NoticeReadLog, Integer> {

    boolean existsByMessageIdAndUserId(Integer messageId, String userId);
}
