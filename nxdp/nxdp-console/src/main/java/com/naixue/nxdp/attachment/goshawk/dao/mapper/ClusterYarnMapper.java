package com.naixue.nxdp.attachment.goshawk.dao.mapper;

import java.util.List;
import java.util.Map;

import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsCold;
import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsMerge;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ClusterYarnMapper {

    @Select(
            "<script>"
                    + "select ${columns},count(1) cnum from"
                    + "("
                    + "SELECT DATE_FORMAT(finish_time,'%Y-%m-%d') dayTime,"
                    + "app_schedule_type,author_name,map_task_number,reduce_task_number,execute_time,queue_name FROM t_cluster_yarn_mapreduce "
                    + " where 1 = 1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + "<if test='startSecs != null'> and execute_time &gt;= #{startSecs} </if>"
                    + "<if test='endSecs != null'> and execute_time &lt; #{endSecs} </if>"
                    + "<if test='appType != null'> and app_schedule_type = #{appType} </if>"
                    + "<if test='maxMap != null'> and ( map_task_number &gt; #{maxMap} or reduce_task_number &gt; #{maxReduce} )</if>"
                    + ")temp group by ${columns}"
                    + "</script>")
    List<Map<String, Object>> countMrTaskByDay(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("columns") String columns,
            @Param("startSecs") Integer startSecs,
            @Param("endSecs") Integer endSecs,
            @Param("appType") String appType,
            @Param("maxMap") Integer maxMap,
            @Param("maxReduce") Integer maxReduce);

    @Select(
            "<script>"
                    + "select * from t_cluster_yarn_mapreduce "
                    + "where 1=1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + "<if test='appType != null'> and app_schedule_type = #{appType} </if>"
                    + "</script>")
    List<Map<String, Object>> queryMrListByTime(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("appType") String appType);

    @Select(
            "<script>"
                    + "select * from t_cluster_yarn_spark "
                    + "where 1=1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + "<if test='appType != null'> and app_schedule_type = #{appType} </if>"
                    + "</script>")
    List<Map<String, Object>> querySparkListByTime(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("appType") String appType);

    @Select(
            "<script>"
                    + "select ${columns},count(1) cnum from"
                    + "("
                    + "SELECT DATE_FORMAT(finish_time,'%Y-%m-%d') dayTime,"
                    + "app_schedule_type,author_name,executor_memtory,executor_memotry_total,executor_cpucore_total,execute_time,queue_name FROM t_cluster_yarn_spark "
                    + " where 1 = 1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + "<if test='startSecs != null'> and execute_time &gt;= #{startSecs} </if>"
                    + "<if test='endSecs != null'> and execute_time &lt; #{endSecs} </if>"
                    + "<if test='appType != null'> and app_schedule_type = #{appType} </if>"
                    + "<if test='maxMem != null'> and ( executor_memtory  &gt; #{maxMem} or executor_memotry_total  &gt; #{maxMemTotal} or executor_cpucore_total > #{maxCpu})</if>"
                    + ")temp group by ${columns}"
                    + "</script>")
    List<Map<String, Object>> countSparkTaskByDay(
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("columns") String columns,
            @Param("startSecs") Integer startSecs,
            @Param("endSecs") Integer endSecs,
            @Param("appType") String appType,
            @Param("maxMem") Integer maxMem,
            @Param("maxMemTotal") Integer maxMemTotal,
            @Param("maxCpu") Integer maxCpu);

    @Select(
            "<script>"
                    + "select threshold_key,threshold_value from t_cluster_threshold "
                    + "where threshold_key = #{thresholdKey} "
                    + "</script>")
    Map<String, Object> getThresholdByColumn(@Param("thresholdKey") String thresholdKey);

    @Select("<script>" + "select distinct hadoop_user_name from t_hadoop_user " + "</script>")
    List<String> getHdfsTeam();

    @Select(
            "<script>"
                    + "select dt,dir,sum(filesize) filesize from "
                    + " (select dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,filesize from t_cluster_hdfs_used where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) "
          /*+"<if test='subDir != null'> and subDir is not null</if>"
          +"<if test='subDir = null'> and subDir is null</if>"*/
                    + " and sub_dir is null ) temp "
                    + " group by dir,dt"
                    + "</script>")
    List<Map<String, Object>> getHdfsReport(
            @Param("dirStr") String dirStr,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Select(
            "<script>"
                    + "select dt,dir,sum(little_num) little_num,sum(file_num) file_num from "
                    + " (select dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,little_num,file_num from t_cluster_hdfs_little_file where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) "
          /*+"<if test='subDir != null'> and subDir is not null</if>"
          +"<if test='subDir = null'> and subDir is null</if>"*/
                    + " and sub_dir is null ) temp "
                    + " group by dir,dt"
                    + "</script>")
    List<Map<String, Object>> getHdfsLittleReport(
            @Param("dirStr") String dirStr,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Select(
            "<script>"
                    + "update t_cluster_hdfs_cold set is_del = #{isDel},error = #{error} where id in (${ids}) "
                    + "</script>")
    void updateColdDataById(
            @Param("isDel") Integer isDel, @Param("error") String error, @Param("ids") String ids);

    @Select(
            "<script>"
                    + "select id,dir,atime,is_del,error from t_cluster_hdfs_cold where 1 = 1 "
                    + "<if test='ids != null'> and id in (${ids}) </if>"
                    + "<if test='isDel != null'> and is_del = #{isDel}</if>"
                    + "<if test='coldDir != null'> and dir like CONCAT('%',#{coldDir},'%')</if>"
                    + "<if test='start != null'> limit #{start},#{size} </if>"
                    + "</script>")
    List<Map<String, Object>> queryColdDataList(
            @Param("isDel") Integer isDel,
            @Param("coldDir") String coldDir,
            @Param("ids") String ids,
            @Param("start") Integer start,
            @Param("size") Integer size);

    @Select(
            "<script>"
                    + "select count(1) from t_cluster_hdfs_cold where 1 = 1 "
                    + "<if test='ids != null'> and id in (${ids}) </if>"
                    + "<if test='isDel != null'> and is_del = #{isDel}</if>"
                    + "<if test='coldDir != null'> and dir like CONCAT('%',#{coldDir},'%')</if>"
                    + "</script>")
    Long countColdDataList(
            @Param("isDel") Integer isDel, @Param("coldDir") String coldDir, @Param("ids") String ids);

    @Select("SELECT * FROM t_cluster_hdfs_cold t WHERE t.id in (#{ids})")
    List<ClusterHdfsCold> findClusterHdfsColdsByIds(@Param("ids") final String ids);

    @Select(
            "<script>"
                    + "select job_state from t_job_execute_log where job_id = #{jobId} order by create_time desc limit 1"
                    + "</script>")
    Integer getColdDataTaskStatus(@Param("jobId") Integer jobId);

    @Select(
            "<script>"
                    + "select dt,dir,sum(num) fileNum from "
                    + " (select dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,filesize num from t_cluster_hdfs_used where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) and sub_dir is null ) temp "
                    + " group by dir,dt order by dt desc "
                    + "<if test='start != null'> limit #{start},#{size} </if>"
                    + "</script>")
    List<Map<String, Object>> getHdfsFileNumList(
            @Param("dirStr") String dirStr,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("start") Integer start,
            @Param("size") Integer size);

    @Select(
            "<script>"
                    + "select dt,dir,sum(num) fileNum from "
                    + " (select dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,little_num num from t_cluster_hdfs_little_file where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) and sub_dir is null ) temp "
                    + " group by dir,dt order by dt desc "
                    + "<if test='start != null'> limit #{start},#{size} </if>"
                    + "</script>")
    List<Map<String, Object>> getHdfsLittleFileNumList(
            @Param("dirStr") String dirStr,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("start") Integer start,
            @Param("size") Integer size);

    @Select(
            "<script>"
                    + "select dt,sub_dir,sum(filesize) fileNum from "
                    + " (select sub_dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,filesize from t_cluster_hdfs_used where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) and sub_dir is not null "
                    + "<if test='subDir != null'> and sub_dir in (${subDir}) </if>"
                    + ") temp group by sub_dir,dt order by dt desc "
                    + "</script>")
    List<Map<String, Object>> getHdfsSubFileNumList(
            @Param("dirStr") String dirStr,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("subDir") String subDir);

    @Select(
            "<script>"
                    + "select dt,sub_dir,sum(num) fileNum from "
                    + " (select sub_dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,little_num num from t_cluster_hdfs_little_file where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) and sub_dir is not null "
                    + "<if test='subDir != null'> and sub_dir in (${subDir}) </if>"
                    + ") temp group by sub_dir,dt order by dt desc "
                    + "</script>")
    List<Map<String, Object>> getHdfsLittleSubFileNumList(
            @Param("dirStr") String dirStr,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime,
            @Param("subDir") String subDir);

  /*@Select("<script>"+
  		"select count(1) from "
  		+ " (select dir,DATE_FORMAT(dt,'%Y-%m-%d') dt,filesize from t_cluster_hdfs_used where dt &gt;= #{startTime} and dt &lt; #{endTime} and dir in (${dirStr}) ) temp "
  		+ " group by dir,dt"+
  		"</script>")
  List<Map<String,Object>> countHdfsFileNum(@Param("dirStr") String dirStr,@Param("startTime") String startTime,@Param("endTime") String endTime);*/

    @Select(
            "<script>"
                    + "select * from t_cluster_hdfs_merge where 1 = 1"
                    + "<if test='ids != null'> and id in (${ids}) </if>"
                    + "<if test='dirStr != null'> and dir = #{dirStr} </if>"
                    + "<if test='isMerge != null'> and is_merge = #{isMerge} </if> order by num desc "
                    + "</script>")
    List<Map<String, Object>> getLittleDirList(
            @Param("dirStr") String dirStr, @Param("isMerge") Integer isMerge, @Param("ids") String ids);

    @Select(
            "<script>"
                    + "select count(1) from t_cluster_hdfs_merge where 1 = 1"
                    + "<if test='ids != null'> and id in (${ids}) </if>"
                    + "<if test='dirStr != null'> and dir = #{dirStr} </if>"
                    + "<if test='isMerges != null'> and is_merge in (${isMerges}) </if> order by num desc "
                    + "</script>")
    Long countLittleDirList(
            @Param("dirStr") String dirStr, @Param("isMerges") String isMerges, @Param("ids") String ids);

    @Select(
            "<script>"
                    + "update t_cluster_hdfs_merge set is_merge = #{isMerge},error = #{error} where id in (${ids}) "
                    + "</script>")
    void updateLittleDirById(
            @Param("isMerge") Integer isMerge, @Param("error") String error, @Param("ids") String ids);

    @Insert(
            "insert into t_cluster_hdfs_merge (dir, num, file_num,is_merge) "
                    + " values "
                    + "(#{dir},#{num},#{fileNum},#{isMerge}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    boolean insertLittleDir(ClusterHdfsMerge clusterHdfsMerge);

    @Select(
            "<script>"
                    + "select finish_time dayTime,count(1) cnum from t_cluster_report_mapreduce "
                    + "where 1=1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + " group by finish_time "
                    + "</script>")
    List<Map<String, Object>> queryLongTaskReport(
            @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select(
            "<script>"
                    + "select finish_time dayTime,count(1) cnum from t_cluster_report_overonhour "
                    + "where 1=1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + " group by finish_time "
                    + "</script>")
    List<Map<String, Object>> queryLongTimeTaskReport(
            @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select(
            "<script>"
                    + "select finish_time dayTime,count(1) cnum from t_cluster_report_spark_overresource "
                    + "where 1=1 "
                    + "<if test='startTime != null'> and finish_time &gt;= #{startTime} </if>"
                    + "<if test='endTime != null'> and finish_time &lt; #{endTime} </if>"
                    + " group by finish_time "
                    + "</script>")
    List<Map<String, Object>> queryLongResourceReport(
            @Param("startTime") String startTime, @Param("endTime") String endTime);

    @Select(
            "<script>"
                    + "select * from t_cluster_flume_manage where 1 = 1 "
                    + "<if test='urlStr != null'> and url_str like #{urlStr}</if>"
                    + "<if test='rangeStr != null'> and range_str like #{rangeStr}</if>"
                    + "<if test='start != null'> limit #{start},#{size} </if>"
                    + "</script>")
    List<Map<String, Object>> queryFlumeManageList(
            @Param("urlStr") String urlStr,
            @Param("rangeStr") String rangeStr,
            @Param("start") Integer start,
            @Param("size") Integer size);

    @Select(
            "<script>"
                    + "select count(1) from t_cluster_flume_manage where 1 = 1 "
                    + "<if test='urlStr != null'> and url_str like #{urlStr}</if>"
                    + "<if test='rangeStr != null'> and range_str like #{rangeStr}</if>"
                    + "</script>")
    Long countFlumeManageList(@Param("urlStr") String urlStr, @Param("rangeStr") String rangeStr);

    @Select(
            "<script>"
                    + "update t_cluster_flume_manage set url_str = #{urlStr},range_str = #{rangeStr},create_user = #{crUser} where id = #{id} "
                    + "</script>")
    void updateFlumeManage(
            @Param("urlStr") String urlStr,
            @Param("rangeStr") String rangeStr,
            @Param("crUser") String crUser,
            @Param("id") Integer id);

    @Insert(
            "insert into t_cluster_flume_manage (url_str, range_str, create_user) "
                    + " values "
                    + "(#{urlStr},#{rangeStr},#{crUser}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    boolean insertFlumeManage(
            @Param("urlStr") String urlStr,
            @Param("rangeStr") String rangeStr,
            @Param("crUser") String crUser);

    @Delete("<script>" + "delete from t_cluster_flume_manage where id= #{id} " + "</script>")
    boolean deleteFlumeManage(@Param("id") Integer id);

    @Select("<script>" + "select * from t_cluster_flume_manage where id = #{fid} " + "</script>")
    Map<String, Object> getFlumeManageDetail(@Param("fid") Integer fid);

    @Select(
            "<script>"
                    + "select * from t_cluster_stream_alarm where 1 = 1 "
                    + "<if test='taskId != null'> and task_id like #{taskId}</if>"
                    + "<if test='groupId != null'> and group_id like #{groupId}</if>"
                    + "<if test='project != null'> and project like #{project}</if>"
                    + "<if test='developUser != null'> and develop_user like #{developUser}</if>"
                    + "<if test='rangeStr != null'> and range_str like #{rangeStr}</if>"
                    + "<if test='start != null'> limit #{start},#{size} </if>"
                    + "</script>")
    List<Map<String, Object>> queryStreamAlarmList(
            @Param("taskId") String taskId,
            @Param("groupId") String groupId,
            @Param("project") String project,
            @Param("developUser") String developUser,
            @Param("rangeStr") String rangeStr,
            @Param("start") Integer start,
            @Param("size") Integer size);

    @Select(
            "<script>"
                    + "select count(1) from t_cluster_stream_alarm where 1 = 1 "
                    + "<if test='taskId != null'> and task_id like #{taskId}</if>"
                    + "<if test='groupId != null'> and group_id like #{groupId}</if>"
                    + "<if test='project != null'> and project like #{project}</if>"
                    + "<if test='developUser != null'> and develop_user like #{developUser}</if>"
                    + "<if test='rangeStr != null'> and range_str like #{rangeStr}</if>"
                    + "</script>")
    Long countStreamAlarmList(
            @Param("taskId") String taskId,
            @Param("groupId") String groupId,
            @Param("project") String project,
            @Param("developUser") String developUser,
            @Param("rangeStr") String rangeStr);

    @Select(
            "<script>"
                    + "update t_cluster_stream_alarm set task_id = #{taskId},group_id = #{groupId},project = #{project},develop_user = #{developUser},range_str = #{rangeStr}"
                    + " where id = #{id} "
                    + "</script>")
    void updateStreamAlarm(
            @Param("taskId") String taskId,
            @Param("groupId") String groupId,
            @Param("project") String project,
            @Param("developUser") String developUser,
            @Param("rangeStr") String rangeStr,
            @Param("id") Integer id);

    @Insert(
            "insert into t_cluster_stream_alarm (task_id, group_id, project,develop_user,range_str,create_user) "
                    + " values "
                    + "(#{taskId},#{groupId},#{project},#{developUser},#{rangeStr},#{createUser}) ")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    boolean insertStreamAlarm(
            @Param("taskId") String taskId,
            @Param("groupId") String groupId,
            @Param("project") String project,
            @Param("developUser") String developUser,
            @Param("rangeStr") String rangeStr,
            @Param("createUser") String createUser);

    @Delete("<script>" + "delete from t_cluster_stream_alarm where id= #{sid} " + "</script>")
    boolean deleteStreamAlarm(@Param("sid") Integer sid);

    @Select("<script>" + "select * from t_cluster_stream_alarm where id = #{sid} " + "</script>")
    Map<String, Object> getStreamAlarmDetail(@Param("sid") Integer sid);
}
