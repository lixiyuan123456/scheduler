package com.naixue.dp.util;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by sunzhiwei on 2018/1/20.
 */
public class HttpUtil {
    private static Logger logger = Logger.getLogger(HttpUtil.class);
    /**
     * 无参数请求
     */
    public static String sendGet(String url) throws IOException {
        return sendGet(url, null);
    }
    /**
     * 向指定URL发送GET方法的请求
     *
     * @param url
     *            发送请求的URL
     * @param param
     *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
     * @return URL 所代表远程资源的响应结果
     */
    public static String sendGet(String url, String param) throws IOException {
        String result = "";
        BufferedReader in = null;

        String urlNameString = null;
        if (param == null || param.length() == 0){
            urlNameString = url;
        }else {
            urlNameString =url + "?" + param;
        }
        URL realUrl = new URL(urlNameString);
        // 打开和URL之间的连接
        URLConnection connection = realUrl.openConnection();
        // 设置通用的请求属性
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("user-agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
        // 建立实际的连接
        connection.connect();
        // 定义 BufferedReader输入流来读取URL的响应
        in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        if (in != null) {
            in.close();
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        HttpUtil httpUtil = new HttpUtil();
        String result = httpUtil.sendGet("http://192.168.187.245:8088/ws/v1/cluster/metrics");
        System.out.println(result);
    }
}
