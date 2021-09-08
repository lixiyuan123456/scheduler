package com.naixue.nxdp.service;

import java.util.List;
import java.util.Map;

import com.naixue.nxdp.model.NoticeConfig;

/**
 * @author: wangyu @Created by 2018/2/5
 */
public interface AnnouncementService {

    /**
     * 查询未过期消息明细，以及读取当前用户查看情况
     *
     * @param userId
     * @return
     */
    List<Map<String, Object>> getMessageList(String userId);

    /**
     * 查询指定消息，并保存已读记录
     *
     * @param msgId  消息ID
     * @param userId 用户ID
     * @return
     */
    NoticeConfig viewMessage(Integer msgId, String userId);
}
