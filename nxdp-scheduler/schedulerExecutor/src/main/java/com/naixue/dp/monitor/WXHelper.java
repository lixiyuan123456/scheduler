package com.naixue.dp.monitor;

/**
 * Created by sunzhiwei on 2018/7/3.
 */

import com.alibaba.fastjson.JSON;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;


public class WXHelper {

    private static Logger logger = Logger.getLogger(WXHelper.class);

    private static final String WX_MESSAGE_SEND_HTTP_URL =
            "http://wxmsg.zhuaninc.com/api/message/send";

    public static void asyncSendWXMsg(final String msg, final String... userNames) {
        new Thread(
                new Runnable() {
                    public void run() {
                        sendWXMsg(new WXMsg(WXMsg.Type.MESSAGE.getCode(), msg), userNames);
                    }
                })
                .start();
    }

    public static void asyncSendWXAlarmMsg(final String msg, final String... userNames) {
        new Thread(
                new Runnable() {
                    public void run() {
                        sendWXMsg(new WXMsg(WXMsg.Type.ALARM.getCode(), msg), userNames);
                    }
                })
                .start();
    }

    private static void sendWXMsg(WXMsg wxMsg, String... userNames) {
        if (userNames == null || userNames.length == 0) {
            throw new IllegalArgumentException("企业微信消息发送对象不允许为空");
        }
        StringBuilder to = new StringBuilder();
        for (int i = 0; i < userNames.length; i++) {
            if (i == userNames.length - 1) {
                to.append(userNames[i]);
            } else {
                to.append(userNames[i] + "|");
            }
        }
        Map<String, Object> object = new HashMap<String, Object>();
        object.put("to_user", to.toString());
        object.put("msg_type", wxMsg.getMsgType());
        object.put("app_code", wxMsg.getAppCode());
        object.put("msg", wxMsg.getMsg());
        object.put("link_url", wxMsg.getLinkUrl());
        try {
            String json = JSON.toJSONString(object);
            logger.debug("企业微信消息request=" + json);
            String resp = HttpSubmitUtil.setPost(WX_MESSAGE_SEND_HTTP_URL, json);
            logger.debug("企业微信消息response=" + resp);
        } catch (Exception e) {
            logger.error(e.toString() + "", e);
        }
    }

    public static void main(String[] args) {
//        WXHelper.sendWXMsg(
//                new WXMsg(WXMsg.Type.MESSAGE.getCode(), "测试"), "sunzhiwei", "zhaichuancheng");
        WXHelper.asyncSendWXMsg("测试WX", "sunzhiwei");
    }
}

