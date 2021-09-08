package com.naixue.dp.monitor;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sunzhiwei on 2018/1/25.
 */

public class MessageUtil {

    /**
     * 发送短信公用方法
     * @param message 信息内容
     * @param telephone 收信人手机号码。多个人用逗号隔开
     */
    public static void sendMsg(String message, String telephone) {
        message = "[zzdp]" + message;
        try {
            message = URLEncoder.encode(message, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = "http://ms.web.58dns.org/sms/warn?function=1011&ms=" + message + "&mo=" + telephone + "&priority=G";
        HttpSubmitUtil.sendGet(url, true, false);
    }

    public static void main(String[] args){
        MessageUtil messageUtil = new MessageUtil();
        messageUtil.sendMsg("test zzdp", "18518634936");
    }
}