package com.naixue.nxdp.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.naixue.nxdp.dao.NoticeConfigRepository;
import com.naixue.nxdp.dao.NoticeReadLogRepository;
import com.naixue.nxdp.model.NoticeConfig;
import com.naixue.nxdp.model.NoticeReadLog;
import com.naixue.nxdp.service.AnnouncementService;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: wangyu
 * @Created by 2018/2/5
 **/
@Service
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private NoticeConfigRepository noticeConfigRepository;
    @Autowired
    private NoticeReadLogRepository noticeReadLogRepository;

    @Override
    public List<Map<String, Object>> getMessageList(String userId) {
        if (userId == null || userId.trim().length() == 0) {
            return null;
        }
        List<NoticeConfig> messageList = noticeConfigRepository.findAllByUserId(userId);
        List<Map<String, Object>> messageMapList = new ArrayList<>();
        for (NoticeConfig message : messageList) {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("id", message.getId());
            messageMap.put("message", message.getMessage());
            messageMap.put("title", message.getTitle());
            messageMap.put("interval", message.getInterval());
            messageMap.put("create_time", message.getCreateTime());
            messageMap.put("have_readed", message.getHaveRead());
            messageMap.put("type", message.getType());
            messageMapList.add(messageMap);
        }
        return messageMapList;
    }

    /**
     * 查询指定消息，并保存已读记录
     *
     * @param msgId  消息ID
     * @param userId 用户ID
     * @return
     */
    @Override
    public NoticeConfig viewMessage(Integer msgId, String userId) {
        // 判断消息是否已读
        boolean readFlag = noticeReadLogRepository.existsByMessageIdAndUserId(msgId, userId);
        // 如果未读则插入已读记录
        if (!readFlag) {
            NoticeReadLog readLog = new NoticeReadLog();
            readLog.setUserId(userId);
            readLog.setMessageId(msgId);
            readLog.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
            noticeReadLogRepository.save(readLog);
        }
        // 查询当前消息的信息
        NoticeConfig message = noticeConfigRepository.findOneById(msgId);
        return message;
    }
}
