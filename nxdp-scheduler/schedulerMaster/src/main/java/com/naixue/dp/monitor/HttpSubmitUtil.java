package com.naixue.dp.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * Created by sunzhiwei on 2018/1/26.
 */
public class HttpSubmitUtil {
    static Log log=LogFactory.getLog(HttpSubmitUtil.class);
    static final Pattern reUnicode = Pattern.compile("\\\\u([0-9a-zA-Z]{4})");

    /**
     * get方式获取数据
     * @author
     * @param url  get提交的url
     * @param isUnicode 是否进行unicode解码
     * @param isLarger 返回的数据量是否过大（为true时比较消耗内存，不建议使用）
     * @return
     */
    public static String sendGet(String url,boolean isUnicode,boolean isLarger){
        return sendGetWithParams(url, isUnicode, isLarger, null);
    }

    public static String sendGetWithParams(String url,boolean isUnicode,boolean isLarger, Map<String, String> paramsMap){
        long startTime=System.currentTimeMillis();
        HttpClient client = new HttpClient();
        StringBuffer sb = new StringBuffer();
        InputStream ins = null;
        GetMethod method = new GetMethod(url);
        method.getParams().setContentCharset("utf-8");
        method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET,"utf-8");
        method.addRequestHeader("Content-Type", "text/xml; charset=UTF-8");
        method.addRequestHeader("Accept-Language", "zh-cn,zh;q=0.5");
        if(paramsMap != null && paramsMap.size() >= 0) {
            for (Map.Entry<String, String> entry : paramsMap.entrySet()) {
                method.addRequestHeader(entry.getKey(), entry.getValue());
            }
        }
        client.getParams().setContentCharset("utf-8");
        try {
            int statusCode = client.executeMethod(method);
            if (statusCode == HttpStatus.SC_OK) {
                if(isLarger){
                    sb.append(method.getResponseBodyAsString(Integer.MAX_VALUE));
                }else{
                    ins = method.getResponseBodyAsStream();
                    byte[] b = new byte[16384];
                    int r_len = 0;
                    while ((r_len = ins.read(b)) > 0) {
                        sb.append(new String(b, 0, r_len, method.getResponseCharSet()));
                    }
                }
            }
        }catch(Exception e){
            log.error(e.getMessage(), e);
        } finally {
            method.releaseConnection();
            try {
                if (ins != null) {
                    ins.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            long endTime=System.currentTimeMillis();
            long costTime=endTime-startTime;
            if(costTime>800){
                log.warn("warring!!!! http get cost time is too long,the url is "+url+",the cost time is"+costTime+"ms");
            }
        }
        return isUnicode?unDecode(sb.toString()):sb.toString();
    }

    public static String setPost(String url,boolean isUnicode,Map<String,String> paramMap){
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(url);

        // 创建参数队列
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        if(paramMap!=null&&!paramMap.isEmpty())
            for(String paramKey:paramMap.keySet()){
                formParams.add(new BasicNameValuePair(paramKey, paramMap.get(paramKey)));
            }
        UrlEncodedFormEntity uefEntity;
        try {
            uefEntity = new UrlEncodedFormEntity(formParams, "UTF-8");
            httppost.setEntity(uefEntity);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return isUnicode?unDecode(EntityUtils.toString(entity, "UTF-8")):EntityUtils.toString(entity, "UTF-8");
                }
            } finally {
                response.close();
            }
        } catch (ClientProtocolException e) {
            log.error(e.getMessage(), e);
        } catch (UnsupportedEncodingException e1) {
            log.error(e1.getMessage(), e1);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * 进行javaunicode 解码
     * @author
     * @return
     */
    public static String unDecode(String codeStr){
        if(StringUtils.isBlank(codeStr))
            return null;
        Matcher m = reUnicode.matcher(codeStr);
        StringBuffer sb = new StringBuffer(codeStr.length());
        while (m.find()) {
            m.appendReplacement(sb,
                    Character.toString((char) Integer.parseInt(m.group(1), 16)));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String setPost(String url, String paramJson){
        // 创建默认的httpClient实例.
        CloseableHttpClient httpclient = HttpClients.createDefault();
        // 创建httppost
        HttpPost httppost = new HttpPost(url);

        try {
            StringEntity s = new StringEntity(paramJson,"UTF-8");
            s.setContentType("application/json");//发送json数据需要设置contentType
            httppost.setEntity(s);
            CloseableHttpResponse response = httpclient.execute(httppost);
            try {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    return EntityUtils.toString(entity, "UTF-8");
                }
            } finally {
                response.close();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接,释放资源
            try {
                httpclient.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
