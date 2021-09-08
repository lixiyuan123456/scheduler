package com.naixue.dp.monitor;

/**
 * Created by sunzhiwei on 2018/7/3.
 */
public class WXMsg {

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

        Type(Integer code, String name) {
            this.code = code;
            this.name = name;
        }

        private Integer code;

        private String name;

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

    public static String getDefaultAppCode() {
        return DEFAULT_APP_CODE;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Integer getMsgType() {
        return msgType;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }
}
