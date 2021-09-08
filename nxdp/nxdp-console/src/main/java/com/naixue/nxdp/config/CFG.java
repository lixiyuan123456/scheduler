package com.naixue.nxdp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"classpath:/profiles/${spring.profiles.active}/cfg.properties"})
public class CFG {

    public static final String DEFAULT_TEMP_FILE_DIRECTORY = "/temp/";
    public static final String ZZ_URL_SSO_QRCODE =
            "https://open.work.weixin.qq.com/wwopen/sso/qrConnect?appid=ww8469a6417268da6f&agentid=1000031&redirect_uri=http%3A%2F%2Fzzdp.zhuanspirit.com";

    // public static String HDP_URL;

    // public static String MAPREDUCE_MONITOR_URL;
    public static final String ZZ_URL_GET_ALL_DEPTS =
            "http://zzauth.zhuanspirit.com/client/getDepartments?systemid=113";

    // public static String WEBHDFS_URL_PREFIX;
    public static final String ZZ_URL_GET_DEPTNAME_BY_DEPTID =
            "http://zzauth.zhuanspirit.com/client/getDepartmentName?systemid=113&departmentid={0}";
    public static final String ZZ_URL_GET_ACCOUNTS_FROM_ONEDEPT =
            "http://zzauth.zhuanspirit.com/client/getDepartmentUsers?systemid=113&departmentid={0}";
    public static final String ZZ_URL_GET_ACCOUNT_BY_ACCOUNT_ID =
            "http://zzauth.zhuanspirit.com/client/get_user_info?uid=%s&systemid=113";
    public static String ZZDP_URL;
    public static String HUE_URL;
    public static String WEBHDFS_ROOT_PATH;
    public static String WEBHDFS_UPLOAD_URL;

    // public static String REALTIME_MONITOR_URL;
    public static String WEBHDFS_UPLOAD_URL_BACK;
    public static String WEBHDFS_DOWNLOAD_URL;
    public static String WEBHDFS_DOWNLOAD_URL_BACK;
    public static String HADOOP_JOB_EXECUTE_LOG_FILE_PATH;
    public static String WEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL;
    public static String HDFS_CLUSTER_URL_PREFIX;
    public static String YARN;

    public static String ZSTREAM_JOB;

    public static String ZSTREAM_JOB_LOG;

    @Value("${zzdp_url}")
    public void setZZDP_URL(String zZDP_URL) {
        ZZDP_URL = zZDP_URL;
    }

    @Value("${hue_url}")
    public void setHUE_URL(String hUE_URL) {
        HUE_URL = hUE_URL;
    }

  /*@Value("${hdp_url}")
  public void setHDP_URL(String hDP_URL) {
    HDP_URL = hDP_URL;
  }*/

  /*@Value("${mapreduce_monitor_url}")
  public void setMAPREDUCE_MONITOR_URL(String mAPREDUCE_MONITOR_URL) {
    MAPREDUCE_MONITOR_URL = mAPREDUCE_MONITOR_URL;
  }*/

    @Value("${webhdfs_root_path}")
    public void setWEBHDFS_ROOT_PATH(String wEBHDFS_ROOT_PATH) {
        WEBHDFS_ROOT_PATH = wEBHDFS_ROOT_PATH;
    }

    @Value("${webhdfs_upload_url}")
    public void setWEBHDFS_UPLOAD_URL(String wEBHDFS_UPLOAD_URL) {
        WEBHDFS_UPLOAD_URL = wEBHDFS_UPLOAD_URL;
    }

    @Value("${webhdfs_upload_url_back}")
    public void setWEBHDFS_UPLOAD_URL_BACK(String wEBHDFS_UPLOAD_URL_BACK) {
        WEBHDFS_UPLOAD_URL_BACK = wEBHDFS_UPLOAD_URL_BACK;
    }

    @Value("${webhdfs_download_url}")
    public void setWEBHDFS_DOWNLOAD_URL(String wEBHDFS_DOWNLOAD_URL) {
        WEBHDFS_DOWNLOAD_URL = wEBHDFS_DOWNLOAD_URL;
    }

    @Value("${webhdfs_download_url_back}")
    public void setWEBHDFS_DOWNLOAD_URL_BACK(String wEBHDFS_DOWNLOAD_URL_BACK) {
        WEBHDFS_DOWNLOAD_URL_BACK = wEBHDFS_DOWNLOAD_URL_BACK;
    }

    @Value("${hadoop_job_execute_log_file_path}")
    public void setHADOOP_JOB_EXECUTE_LOG_FILE_PATH(String hADOOP_JOB_EXECUTE_LOG_FILE_PATH) {
        HADOOP_JOB_EXECUTE_LOG_FILE_PATH = hADOOP_JOB_EXECUTE_LOG_FILE_PATH;
    }

    @Value("${websocket_hadoop_job_execute_log_reader_url}")
    public void setWEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL(
            String wEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL) {
        WEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL = wEBSOCKET_HADOOP_JOB_EXECUTE_LOG_READER_URL;
    }

    @Value("${hdfs_cluster_url_prefix}")
    public void setHDFS_CLUSTER_URL_PREFIX(String hDFS_CLUSTER_URL_PREFIX) {
        HDFS_CLUSTER_URL_PREFIX = hDFS_CLUSTER_URL_PREFIX;
    }

  /*@Value("${realtime_monitor_url}")
  public void setREALTIME_MONITOR_URL(String rEALTIME_MONITOR_URL) {
    REALTIME_MONITOR_URL = rEALTIME_MONITOR_URL;
  }*/

    @Value("${yarn}")
    public void setYARN(String yARN) {
        YARN = yARN;
    }

    @Value("${zstream_job}")
    public void setZSTREAM_JOB(String zSTREAM_JOB) {
        ZSTREAM_JOB = zSTREAM_JOB;
    }

    @Value("${zstream_job_log}")
    public void setZSTREAM_JOB_LOG(String zSTREAM_JOB_LOG) {
        ZSTREAM_JOB_LOG = zSTREAM_JOB_LOG;
    }
}
