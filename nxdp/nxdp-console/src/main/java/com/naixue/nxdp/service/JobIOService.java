package com.naixue.nxdp.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.naixue.nxdp.dao.JobIORepository;
import com.naixue.nxdp.model.JobIO;

@Service
public class JobIOService {

    public static final String EMPTY_ARRAY = "[]";

    public static final String SEPARATOR = ",";

    @Autowired
    private JobIORepository jobIORepository;

    @Transactional
    public void parseConfigToJobIOs(
            final Integer jobId, final JobIO.Type type, final JobIO.Mode mode, final String config) {
        Assert.notNull(jobId, "jobId is not allowed to be null.");
        Assert.notNull(type, "type is not allowed to be null.");
        Assert.notNull(mode, "mode is not allowed to be null.");
        Assert.notNull(config, "config is not allowed to be null.");
        List<String> values = new ArrayList<>();
        if (mode == JobIO.Mode.DATATABLE) {
            if (Strings.isNullOrEmpty(config) || config.equals(EMPTY_ARRAY)) {
                return;
            }
            List<String> jsonStrings = JSON.parseArray(config, String.class);
            if (CollectionUtils.isEmpty(jsonStrings)) {
                return;
            }
            for (String jsonString : jsonStrings) {
                JSONObject jsonObject = JSON.parseObject(jsonString);
                values.add(jsonObject.getString("id"));
            }
        }
        if (mode == JobIO.Mode.HDFS) {
            if (Strings.isNullOrEmpty(config)) {
                return;
            }
            values = Splitter.on(SEPARATOR).splitToList(config);
        }
        if (values.size() == 0) {
            return;
        }
        List<JobIO> jobIOs = new ArrayList<>();
        for (String value : values) {
            JobIO jobIO = JobIO.builder().jobId(jobId).enumType(type).enumMode(mode).value(value).build();
            jobIOs.add(jobIO);
        }
        jobIORepository.deleteByJobId(jobId);
        jobIORepository.save(jobIOs);
    }

    public List<JobIO> getJobIOsByJobId(final Integer jobId) {
        return jobIORepository.findByJobId(jobId);
    }
}
