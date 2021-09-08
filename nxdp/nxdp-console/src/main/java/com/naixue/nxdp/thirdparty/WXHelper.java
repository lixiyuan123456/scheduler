package com.naixue.nxdp.thirdparty;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.naixue.nxdp.util.HttpUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WXHelper {

    private static final String WX_MESSAGE_SEND_HTTP_URL =
            "http://wxmsg.zhuaninc.com/api/message/send";

    public static void asyncSendWXMsg(String msg, String... userNames) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        sendWXMsg(new WXMsg(WXMsg.Type.MESSAGE.getCode(), msg), userNames);
                    }
                })
                .start();
    }

    public static void asyncSendWXAlarmMsg(String msg, String... userNames) {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        sendWXMsg(new WXMsg(WXMsg.Type.ALARM.getCode(), msg), userNames);
                    }
                })
                .start();
    }

    private static void sendWXMsg(WXMsg wxMsg, String... userNames) {
        Assert.notNull(wxMsg, "请求参数wxMsg不允许为空");
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
        Map<String, Object> object = new HashMap<>();
        object.put("to_user", to.toString());
        object.put("msg_type", wxMsg.getMsgType());
        object.put("app_code", wxMsg.getAppCode());
        object.put("msg", wxMsg.getMsg());
        object.put("link_url", wxMsg.getLinkUrl());
        try {
            String json = JSON.toJSONString(object);
            log.debug("企业微信消息request=" + json);
            String resp = executePost(WX_MESSAGE_SEND_HTTP_URL, json);
            log.debug("企业微信消息response=" + resp);
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
    }

    private static String executePost(String url, String json) throws Exception {
        Request request = Request.Post(url).body(new StringEntity(json, ContentType.APPLICATION_JSON));
        request.socketTimeout(HttpUtils.HTTP_SOCKET_TIMEOUT);
        request.connectTimeout(HttpUtils.HTTP_CONNECT_TIMEOUT);
        return request.execute().returnContent().asString(Charset.defaultCharset());
    }

    public static void main(String[] args) {
        WXHelper.sendWXMsg(
                new WXMsg(WXMsg.Type.MESSAGE.getCode(), "测试"), "zhaichuancheng", "zhaichuancheng");
    }

    @Data
    public static class WXMsg {

        private static final String DEFAULT_APP_CODE = "827c4790af4ca7a5ba2db47a72099df5";

        private String appCode = DEFAULT_APP_CODE; // appCode
        private Integer msgType;
        private String msg; // 消息信息
        private String linkUrl; // 微信跳转链接地址

        public WXMsg(String msg) {
            this.msgType = Type.MESSAGE.getCode();
            this.msg = msg;
        }

        public WXMsg(Integer msgType, String msg) {
            this.msgType = msgType;
            this.msg = msg;
        }

        public WXMsg(Integer msgType, String msg, String linkUrl) {
            super();
            this.msgType = msgType;
            this.msg = msg;
            this.linkUrl = linkUrl;
        }

        public static enum Type {
            ALARM(0, "报警"),

            MESSAGE(1, "消息");

            private Integer code;
            private String name;

            Type(Integer code, String name) {
                this.code = code;
                this.name = name;
            }

            public Integer getCode() {
                return code;
            }

            public void setCode(Integer code) {
                this.code = code;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }
        }
    }
}
