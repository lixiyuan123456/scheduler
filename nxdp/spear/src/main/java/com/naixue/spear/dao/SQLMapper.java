package com.naixue.spear.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SQLMapper {

    @Select(
            "select job_id, choose_run_time, id, type, retry, retry_time_span, job_name, update_time from t_job_execute_log where job_state = #{status}")
    List<Map<String, Object>> findByStatus(@Param("status") int status) throws Exception;
}
