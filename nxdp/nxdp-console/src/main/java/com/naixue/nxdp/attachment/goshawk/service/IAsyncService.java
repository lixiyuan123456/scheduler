package com.naixue.nxdp.attachment.goshawk.service;

import java.util.List;
import java.util.Map;

public interface IAsyncService {

    /**
     * @author wangkaixuan
     * @date 2018年12月17日 @Description:删除冷数据
     */
    void delColdDir(List<Map<String, Object>> pathList) throws Exception;

    void delColdDir(List<Map<String, Object>> pathList, boolean force) throws Exception;

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:合并小文件
     */
    void mergeLittleDir(List<Map<String, Object>> pathList) throws Exception;
}
