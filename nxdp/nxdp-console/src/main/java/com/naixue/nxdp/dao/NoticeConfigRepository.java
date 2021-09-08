package com.naixue.nxdp.dao;

import java.util.List;

import com.naixue.nxdp.model.NoticeConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * @author: wangyu @Created by 2018/1/29
 */
public interface NoticeConfigRepository extends JpaRepository<NoticeConfig, Integer> {

    //    @Query(value = "select config " +
    //            "from NoticeConfig as config left join config.noticeReadLog as log " +
    //            "on log.userId=?1 " +
    //            "where config.isDel=0 and config.expireTime>CURRENT_TIMESTAMP")
    //    List<NoticeConfig> findAllByUserId(String userId);

    @Query(
            value =
                    "select config.*, if(log.id is not null, 1, 0) have_read "
                            + "from t_notice_config as config left join t_notice_read_log as log "
                            + "on config.id=log.message_id and log.user_id=?1 "
                            + "where config.is_del=0 and config.expire_time>CURRENT_TIMESTAMP",
            nativeQuery = true)
    List<NoticeConfig> findAllByUserId(String userId);

    @Query(value = "select *, 0 as have_read from t_notice_config where id=?1", nativeQuery = true)
    NoticeConfig findOneById(Integer id);

    //    List<NoticeConfig> findByIsDelAndExpireTimeAfterAndNoticeReadLog_UserId(Integer isDel,
    // Timestamp expireTime, String userId);
}
