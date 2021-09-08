package com.naixue.nxdp.service;

import java.io.InputStream;

import javax.servlet.http.HttpServletResponse;

import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.google.common.net.UrlEscapers;
import com.naixue.nxdp.config.CFG;
import com.naixue.nxdp.util.FileUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebHDFSService {

    private static final String WEBHDFS_UPLOAD_URL = CFG.WEBHDFS_UPLOAD_URL;

    private static final String WEBHDFS_UPLOAD_URL_BACK = CFG.WEBHDFS_UPLOAD_URL_BACK;

    private static final String WEBHDFS_DOWNLOAD_URL = CFG.WEBHDFS_DOWNLOAD_URL;

    private static final String WEBHDFS_DOWNLOAD_URL_BACK = CFG.WEBHDFS_DOWNLOAD_URL_BACK;

    private static String renameFileName(String filename) {
        String namePrefix = "";
        String nameSuffix = "";
        int pointIndex = filename.lastIndexOf(".");
        long timestamp = System.currentTimeMillis();
        if (pointIndex >= 0) {
            namePrefix = filename.substring(0, pointIndex);
            nameSuffix = filename.substring(pointIndex);
        } else {
            namePrefix = filename;
        }
        String newFilename = namePrefix + "_" + timestamp + nameSuffix;
        return newFilename;
    }

    public static void main(String[] args) {
        String s = UrlEscapers.urlFragmentEscaper().escape("/home/skynet/sql/自定义变量.sql");
        System.out.println(s);
    }

    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 1,
            backoff = @Backoff(value = 1000))
    public String uploadFileStreamToWebHDFS(String rootDir, String fileName, InputStream in) {
    /*final String realFileName = renameFileName(fileName);
    String newFileName;
    try {
      newFileName = URLEncoder.encode(realFileName, Charset.defaultCharset().toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    final String newUri = rootDir + newFileName;*/
        final String realFilePath = rootDir + renameFileName(fileName);
        try {
            final String url = String.format(WEBHDFS_UPLOAD_URL, realFilePath);
            log.info(url);
            FileUtils.uploadFileStreamToWebHDFS(url, in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return realFilePath;
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 1,
            backoff = @Backoff(value = 1000))
    public String uploadScriptStringToWebHDFS(String rootDir, String fileName, String script) {
        final String realFilePath = rootDir + fileName;
    /*String filePath;
    try {
      filePath = rootDir + URLEncoder.encode(fileName, Charset.defaultCharset().toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }*/
        try {
            final String url = String.format(WEBHDFS_UPLOAD_URL, realFilePath);
            log.info(url);
            FileUtils.uploadScriptStringToWebHDFS(url, script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return realFilePath;
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 1,
            backoff = @Backoff(value = 1000))
    public void downloadFileFromWebHDFS(
            HttpServletResponse response, String fileName, String realFileName) {
        final String newPath = UrlEscapers.urlFragmentEscaper().escape(fileName);
        try {
            final String url = String.format(WEBHDFS_DOWNLOAD_URL, newPath);
            log.info(url);
            FileUtils.downloadFileFromWebHDFS(response, url, realFileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 1,
            backoff = @Backoff(value = 1000))
    public String readScriptStringFromWebHDFS(String path, String revision) {
        final String newPath = UrlEscapers.urlFragmentEscaper().escape(path);
        try {
            final String url = String.format(WEBHDFS_DOWNLOAD_URL, newPath + "@" + revision);
            log.info(url);
            return FileUtils.readScriptStringFromWebHDFS(url);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Recover
    public String recover(Exception e) {
        throw new RuntimeException(e);
    }

    @Recover
    public String recover(RuntimeException e) {
        throw new RuntimeException(e);
    }
}
