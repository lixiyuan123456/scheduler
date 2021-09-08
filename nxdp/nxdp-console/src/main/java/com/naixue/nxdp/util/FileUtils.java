package com.naixue.nxdp.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSON;
import com.google.common.net.UrlEscapers;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtils {

    public static void uploadToHDFS(String url, MultipartFile file) {
        log.info("url={},fileName={}", url, file.getName());
        final String newUrl = UrlEscapers.urlFragmentEscaper().escape(url);
        HttpURLConnection connection = getHttpURLConnection(newUrl);
        try {
            FileCopyUtils.copy(file.getInputStream(), connection.getOutputStream());
            if (connection.getResponseCode() != 201) {
                log.info(JSON.toJSONString(connection.getHeaderFields()));
                throw new RuntimeException("http code:" + connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.toString(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void uploadFileStreamToWebHDFS(String url, InputStream file) {
        log.info("url={}", url);
        final String newUrl = UrlEscapers.urlFragmentEscaper().escape(url);
        HttpURLConnection connection = getHttpURLConnection(newUrl);
        try (BufferedOutputStream out = new BufferedOutputStream(connection.getOutputStream());
             BufferedInputStream fileStream = new BufferedInputStream(file);) {
            byte[] buff = new byte[1024];
            for (int len = 0; (len = fileStream.read(buff)) > 0; ) {
                out.write(buff, 0, len);
                out.flush();
            }
            if (connection.getResponseCode() != 201) {
                log.info(JSON.toJSONString(connection.getHeaderFields()));
                throw new RuntimeException("http code:" + connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void uploadScriptStringToWebHDFS(String url, String script) {
        log.info("url={},script={}", url, script);
        final String newUrl = UrlEscapers.urlFragmentEscaper().escape(url);
        HttpURLConnection connection = getHttpURLConnection(newUrl);
        try (OutputStream out = connection.getOutputStream();) {
            byte[] bytes = script.getBytes();
            out.write(bytes, 0, bytes.length);
            out.flush();
            if (connection.getResponseCode() != 201) {
                log.info(JSON.toJSONString(connection.getHeaderFields()));
                throw new RuntimeException("http code:" + connection.getResponseCode());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static void downloadFileFromWebHDFS(
            HttpServletResponse response, String url, String realFileName) {
        HttpURLConnection connection;
        try {
            realFileName = URLEncoder.encode(realFileName, "UTF-8");
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty(
                    "content-type",
                    "application/x-www-form-urlencoded;charset=utf-8"); // 设置请求header的属性--请求内容类型
            connection.setRequestProperty("method", "GET"); // 设置请求header的属性--请求方式
            connection.connect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        // response.reset(); // 清空输出流
        response.addHeader(
                HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + realFileName); // 设定输出文件名
        response.setContentType("application/octet-stream"); // 定义输出类型
        try (OutputStream out = response.getOutputStream();
             BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());) {
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = bis.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static String readScriptStringFromWebHDFS(String url) {
        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty(
                    "content-type",
                    "application/x-www-form-urlencoded;charset=utf-8"); // 设置请求header的属性--请求内容类型
            connection.setRequestProperty("method", "GET"); // 设置请求header的属性--请求方式
            connection.connect();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        try (BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());) {
            StringBuilder script = new StringBuilder();
            byte[] buff = new byte[1024];
            int size = 0;
            while ((size = bis.read(buff)) != -1) {
                script.append(new String(buff, 0, size));
            }
            return script.toString();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private static HttpURLConnection getHttpURLConnection(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("PUT");
            conn.setRequestProperty(HttpHeaders.CONNECTION, "Keep-Alive");
            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "application/octet-stream");
            return conn;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
