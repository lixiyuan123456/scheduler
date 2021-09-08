package com.naixue.nxdp.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.springframework.util.CollectionUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpUtils {

    public static final Integer HTTP_SOCKET_TIMEOUT = 5000;

    public static final Integer HTTP_CONNECT_TIMEOUT = 5000;

    public static String httpGet(String urlStr) {
        StringBuilder resultStr = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.94 Safari/537.36");
            connection.addRequestProperty("Host", "zzdp.zhuanspirit.com");
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                resultStr.append(line).append("\n");
            }
        } catch (MalformedURLException e) {
            log.error("Wrong URL : " + e.getMessage());
        } catch (IOException e) {
            log.error("Connect failed : " + e.getMessage());
        }
        return resultStr.toString();
    }

    public static String executePost(String url, Map<String, Object> paramsMap,
                                     Map<String, Object> headersMap) throws Exception {
        Request request = Request.Post(url).body(addToParams(paramsMap));
        addHeaders(request, headersMap);
        request.socketTimeout(HTTP_SOCKET_TIMEOUT);
        request.connectTimeout(HTTP_CONNECT_TIMEOUT);
        return request.execute().returnContent().asString(Charset.defaultCharset());
    }

    private static HttpEntity addToParams(Map<String, Object> paramsMap) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (!CollectionUtils.isEmpty(paramsMap)) {
            for (Map.Entry<String, Object> entry : paramsMap.entrySet()) {
                builder.addTextBody(entry.getKey(), (String) entry.getValue(),
                        ContentType.create("text/plain", Consts.UTF_8));
            }
        }
        return builder.build();
    }

    private static void addHeaders(Request request, Map<String, Object> headersMap) {
        if (!CollectionUtils.isEmpty(headersMap)) {
            return;
        }
        for (Map.Entry<String, Object> entry : headersMap.entrySet()) {
            request.addHeader(new BasicHeader(entry.getKey(), (String) entry.getValue()));
        }
    }
}
