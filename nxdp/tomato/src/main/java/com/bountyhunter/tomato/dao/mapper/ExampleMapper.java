package com.bountyhunter.tomato.dao.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
// @Qualifier("mybatisPrimarySqlSessionTemplate")
public interface ExampleMapper {

    @Select("select * from t_account")
    List<Map<String, Object>> list();
}
