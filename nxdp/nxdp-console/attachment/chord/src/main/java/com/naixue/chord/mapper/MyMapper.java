package com.naixue.chord.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

public interface MyMapper {

  @Select("select * from t_account")
  List<Object> list();
}
