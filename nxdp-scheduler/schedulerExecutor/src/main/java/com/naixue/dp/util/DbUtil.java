package com.naixue.dp.util;

import org.apache.log4j.Logger;

import java.sql.*;

/**
 * Created by sunzhiwei on 2018/1/20.
 */
public class DbUtil {
    private static Logger logger = Logger.getLogger(DbUtil.class);

    private static final String DB_DRIVER = Configuration.getConfiguration().get("DB_DRIVER");
    private static final String DB_URL = Configuration.getConfiguration().get("DB_URL");
    private static final String DB_USER = Configuration.getConfiguration().get("DB_USER");
    private static final String DB_PWD = Configuration.getConfiguration().get("DB_PWD");

    private Connection connection = null;

    private void getConnection(){
        if (this.connection == null) {
            try
            {
                Class.forName(DB_DRIVER);
                this.connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PWD);
            }catch (Exception e)
            {
                logger.error("connection error : ", e);
            }
        }
    }

    public DbUtil(){
        getConnection();
    }

    public void closeConnection(Statement statement){
        try {
            if (statement != null){
                statement.close();
            }
            if (this.connection != null) {
                this.connection.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage() + "", e);
        }
    }

    public void closeStatement(Statement statement, String sql){
        try {
            if (statement != null){
                statement.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage() + " with - " + sql, e);
        }
    }

    public void closeResultSet(ResultSet resultSet, String sql){
        try {
            if (resultSet != null){
                resultSet.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage() + " with - " + sql, e);
        }

    }

    // 加上sql为了根据那个sql出错容易定位代码
    public Statement createStatement(String sql) {
        Statement  statement = null;
        try {
            getConnection();
            statement = this.connection.createStatement();
        } catch (SQLException e) {
            logger.error(e.getMessage() + " with - " + sql, e);
        }
        return statement;
    }

    public ResultSet executeQuery(Statement statement, String sql){
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery(sql);
        } catch (SQLException e) {
//            logger.error(e.getMessage());
            logger.error(e.getMessage() + " with " + sql, e);
        }
        return resultSet;
    }

    public int executeUpdate(Statement statement, String sql){
        int result = 0;
        try {
            result = statement.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage() + " with " + sql, e);
        }
        return result;
    }

    public static void main(String[] args){
        DbUtil dbUtil = new DbUtil();
//        String selectSql = "select * from t_job_config";
//        Statement statement = dbUtil.createStatement(selectSql);
//        ResultSet resultSet = dbUtil.executeQuery(statement, selectSql);
//        try {
//            ResultSetMetaData metaData = resultSet.getMetaData();
//            while (resultSet.next()){
//                System.out.println(resultSet.getObject(metaData.getColumnName(1)));
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

//        String insertSql = "insert into t_job_dependencies (id, job_id, dependent_job_id) values(1,1,1)";
//        int num = dbUtil.executeUpdate(statement, insertSql);
//        String updateSql = "update t_job_dependencies set job_id=3 where id = 1";
//        int num = dbUtil.executeUpdate(statement, updateSql);
//        dbUtil.closeConnection(statement);
    }
}
