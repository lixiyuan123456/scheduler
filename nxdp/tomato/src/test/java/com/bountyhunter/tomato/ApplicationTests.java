package com.bountyhunter.tomato;

import com.bountyhunter.tomato.dao.mapper.ExampleMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Autowired
    ExampleMapper accountMapper;

    @Autowired
    JdbcTemplate primaryJdbcTemplate;

    @Autowired
    JdbcTemplate viceJdbcTemplate;

    @Test
    public void contextLoads() {
        List<Map<String, Object>> primaryList = primaryJdbcTemplate.queryForList("show databases");
        System.out.println(primaryList.size());
        List<Map<String, Object>> viceList = viceJdbcTemplate.queryForList("show databases");
        System.out.println(viceList.size());
        List<Map<String, Object>> list = accountMapper.list();
        System.out.println(list.size());
    }
}
