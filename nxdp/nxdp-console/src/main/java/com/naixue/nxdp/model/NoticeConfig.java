package com.naixue.nxdp.model;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author: wangyu
 * @Created by 2018/1/29
 **/
@Entity
@Table(name = "t_notice_config")
public class NoticeConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title", length = Integer.MAX_VALUE)
    private String title;

    @Column(name = "message", length = Integer.MAX_VALUE)
    private String message;

    @Column(name = "type")
    private Integer type;

    @Column(name = "creator")
    private String creator;

    @Deprecated
    @Column(name = "receiver_id")
    private String receiverId;

    @Column(name = "effect_start_time")
    private Timestamp effectStartTime;

    @Column(name = "interval")
    private String interval;

    @Column(name = "expire_time")
    private Timestamp expireTime;

    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "update_time")
    private Timestamp updateTime;

    @Column(name = "is_del")
    private Integer isDel;

    @Column(name = "have_read")
    private Integer haveRead;

//    @Column(table = "NoticeReadLog")
//    private NoticeReadLog noticeReadLog;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public Timestamp getEffectStartTime() {
        return effectStartTime;
    }

    public void setEffectStartTime(Timestamp effectStartTime) {
        this.effectStartTime = effectStartTime;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public Timestamp getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Timestamp expireTime) {
        this.expireTime = expireTime;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }
//
//    public NoticeReadLog getNoticeReadLog() {
//        return noticeReadLog;
//    }
//
//    public void setNoticeReadLog(NoticeReadLog noticeReadLog) {
//        this.noticeReadLog = noticeReadLog;
//    }

    public Integer getHaveRead() {
        return haveRead;
    }

    public void setHaveRead(Integer haveRead) {
        this.haveRead = haveRead;
    }
}
