package com.naixue.nxdp.attachment.goshawk.service;

import java.util.List;
import java.util.Map;

import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsMerge;
import org.springframework.data.domain.Pageable;

public interface IGoshawkService {

    Map<String, Object> getAppNumCount(String yes7day, String today, String yesDay, String yesyesDay, String[] xList);


    Map<String, Object> getLongTimeTaskCount(String yes7day, String today, String yesDay, String yesyesDay, String[] xList);


    Map<String, Object> getRunTimePai(String yes7day, String today, String yesDay, String yesyesDay, Integer type);

    Map<String, Object> getHdfsReport(String yes7day, String today, String yesDay, String yesyesDay, String[] xList);

    Map<String, Object> getHdfsLittleDirReport(String yes7day, String today, String yesDay, String yesyesDay, String[] xList, Integer dataType);

    Map<String, Object> getColdDataList(Pageable pageable, Integer isDel, String coldDir);

    Long countColdDataByStatus(Integer isDel, String ids);

    //Map<String,Object> getColdDataByTask(String date);
    List<Map<String, Object>> getDelColdDataList(String ids);

    void updateColdData(String ids, Integer status);

    Integer getColdDataTaskStatus(Integer jobId);


    Map<String, Object> getHdfsFileNumData(Integer menuType);

    Map<String, Object> getHdfsFileDetailNumData(String dirStr, String date, Integer menuType);

    List<Map<String, Object>> getLittleDirList(Integer isMerge, String dirStr, String ids);

    Long countLittleDirList(String isMerges, String dirStr, String ids);

    void updateLittleDir(String ids, Integer isMerge);


    ClusterHdfsMerge saveClusterHdfsMerge(ClusterHdfsMerge clusterHdfsMerge);


    Map<String, Object> getTaskPieReportData();

    Map<String, Object> get24HoursTaskReportData(Integer reprotType, String reportDate);

    Map<String, Object> getDeptJobReprotData(Integer reportType);

    Map<String, Object> getFlumeManageList(Pageable pageable, String urlStr, String rangeStr);

    Map<String, Object> saveOrUpdateFlumeManage(Integer id, String urlStr, String rangeStr, String crUser);

    Map<String, Object> deleteFlumeManage(Integer id);

    Map<String, Object> getFlumeManageDetail(Integer id);


    Map<String, Object> getStreamAlarmList(Pageable pageable, String taskId, String groupId, String project, String developUser, String rangeStr);

    Map<String, Object> saveOrUpdateStreamAlarm(Integer id, String taskId, String groupId, String project, String developUser, String rangeStr, String crUser);

    Map<String, Object> deleteStreamAlarm(Integer id);

    Map<String, Object> getStreamAlarmDetail(Integer id);

}
