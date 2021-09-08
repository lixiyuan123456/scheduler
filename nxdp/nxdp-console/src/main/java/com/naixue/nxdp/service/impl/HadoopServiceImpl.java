package com.naixue.nxdp.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.naixue.nxdp.service.HadoopService;

/**
 * @author: wangyu @Created by 2018/2/6
 */
@Service
public class HadoopServiceImpl implements HadoopService {

    // @Autowired private QueueRepository queueRepository;
    // @Autowired private JobConfigRepository jobConfigRepository;

    /**
     * 获取job重要等级字典信息
     *
     * @return
     */
    @Override
    public List<Map<String, Object>> getJobLevelList() {
        List<Map<String, Object>> jobLevels = new ArrayList<>();
        Map<String, Object> level = new HashMap<>();
        level.put("id", 1);
        level.put("name", "一级");
        jobLevels.add(level);

        level = new HashMap<>();
        level.put("id", 2);
        level.put("name", "二级");
        jobLevels.add(level);

        level = new HashMap<>();
        level.put("id", 3);
        level.put("name", "三级");
        jobLevels.add(level);
        return jobLevels;
    }
}
