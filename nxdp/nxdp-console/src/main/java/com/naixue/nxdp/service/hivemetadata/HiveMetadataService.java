package com.naixue.nxdp.service.hivemetadata;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.Data;

@Service
public class HiveMetadataService {

    @Autowired
    private JdbcTemplate hadoopMetadataJdbcTemplate;

    public List<Db> listDbs() {
        return hadoopMetadataJdbcTemplate.query(
                "SELECT * FROM DBS", BeanPropertyRowMapper.newInstance(Db.class));
    }

    public List<Tbl> listTbls(Integer dbId) {
        return hadoopMetadataJdbcTemplate.query(
                "SELECT t1.DB_ID,t1.TBL_ID,t1.TBL_NAME,t2.LOCATION FROM TBLS t1,SDS t2 WHERE t1.SD_ID = t2.SD_ID AND t1.DB_ID ="
                        + dbId,
                BeanPropertyRowMapper.newInstance(Tbl.class));
    }

    @Data
    public static class Db {

        private Integer dbId;

        private String name;
    }

    @Data
    public static class Tbl {

        private Integer dbId;

        private Integer tblId;

        private String tblName;

        private String location;
    }
}
