package com.naixue.nxdp.util;

import java.text.MessageFormat;

import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YarnUtils {

    public static String CLUSTER_APPLICATION_STATE_API = "{0}/ws/v1/cluster/apps/{1}/state";

    public static String queryApplicationStateById(String endpoint, String applicationId) {
        String url = MessageFormat.format(CLUSTER_APPLICATION_STATE_API, endpoint, applicationId);
        String data = new RestTemplate().getForObject(url, String.class);
        log.info("url={},return={}.", url, data);
        return data;
    }

    public static State queryApplicationStateByApplicationId(String endpoint, String applicationId) {
        String json = queryApplicationStateById(endpoint, applicationId);
        if (!Strings.isNullOrEmpty(json)) {
            String state = JSON.parseObject(json).getString("state");
            return State.valueOf(state);
        }
        return State.OTHER;
    }

    public static enum State {
        RUNNING,
        FAILED,
        KILLED,
        OTHER;
    }
}
