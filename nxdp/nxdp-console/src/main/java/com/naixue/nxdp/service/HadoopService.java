package com.naixue.nxdp.service;

import java.util.List;
import java.util.Map;

/**
 * @author: wangyu @Created by 2018/2/6
 */
public interface HadoopService {

    /**
     * 获取job重要等级字典信息
     *
     * @return
     */
    List<Map<String, Object>> getJobLevelList();
}
