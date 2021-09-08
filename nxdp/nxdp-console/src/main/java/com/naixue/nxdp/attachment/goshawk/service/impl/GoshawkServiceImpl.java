package com.naixue.nxdp.attachment.goshawk.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.naixue.nxdp.attachment.goshawk.dao.mapper.ClusterYarnMapper;
import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsMerge;
import com.naixue.nxdp.attachment.goshawk.service.IGoshawkService;
import com.naixue.nxdp.attachment.util.DateUtil;
import com.naixue.nxdp.util.HttpUtils;

@Service
public class GoshawkServiceImpl implements IGoshawkService {

    static DecimalFormat df = new DecimalFormat("0.00");
    @Autowired
    ClusterYarnMapper clusterYarnMapper;

    /**
     * @author wangkaixuan
     * @date 2018年12月4日 @Description:获取昨天运行量(方块图)
     */
  /*@Override
  public Map<String, String> getAppNum() {

  	Map<String, String> res = new HashMap<>();
  	DecimalFormat df = new DecimalFormat("#.00");

  	//昨天的运行量
  	String yesday = DateUtils.getNDaysAgo(1, "yyyy-MM-dd");
  	String today = DateUtils.getNDaysAgo(0, "yyyy-MM-dd");

  	List<Map<String,Object>> mrNumList = clusterYarnMapper.countMrTaskByDay(yesday, today, "dayTime", null, null, null, null, null);
  	List<Map<String,Object>> sparkNumList = clusterYarnMapper.countSparkTaskByDay(yesday, today, "dayTime", null, null, null, null, null, null);
  	Long mrNum = 0l;
  	Long sparkNum = 0l;
  	if(mrNumList!=null && mrNumList.size()>0) {
  		mrNum = (Long) mrNumList.get(0).get("cnum");
  	}
  	if(sparkNumList!=null && sparkNumList.size()>0) {
  		sparkNum = (Long) sparkNumList.get(0).get("cnum");
  	}
  	Long yesNum = mrNum+sparkNum;

  	//前天运行量
  	String yesyesday = DateUtils.getNDaysAgo(2, "yyyy-MM-dd");
  	List<Map<String,Object>> mrNumList2 = clusterYarnMapper.countMrTaskByDay(yesyesday, yesday, "dayTime", null, null, null, null, null);
  	List<Map<String,Object>> sparkNumList2 = clusterYarnMapper.countSparkTaskByDay(yesyesday, yesday, "dayTime", null, null, null, null, null, null);
  	Long mrNum2 = 0l;
  	Long sparkNum2 = 0l;
  	if(mrNumList2!=null && mrNumList2.size()>0) {
  		mrNum2 = (Long) mrNumList2.get(0).get("cnum");
  	}
  	if(sparkNumList2!=null && sparkNumList2.size()>0) {
  		sparkNum2 = (Long) sparkNumList2.get(0).get("cnum");
  	}

  	Long yesyesNum = mrNum2+sparkNum2;

  	if(yesNum>yesyesNum) {
  		res.put("color", "green-soft");
  	}else {
  		res.put("color", "red-soft");
  	}

  	Double rate = (yesNum*0.00-yesyesNum*0.00)/yesyesNum*0.00;
  	res.put("rate", df.format(rate*100)+"%");
  	res.put("yesNum", yesNum.toString());


  	//昨天超长任务

  	//昨天task过多任务

  	//昨天资源过多


  	return res;
  }*/

    /**
     * @author wangkaixuan
     * @date 2018年12月4日 @Description:获取App日运行量：块状图、折线图、柱状图数据
     */
    @Override
    public Map<String, Object> getAppNumCount(
            String yes7day, String today, String yesDay, String yesyesDay, String[] xList) {

        Map resMap = new HashMap<>();

        Map<String, Long> lineMap = new HashMap<>(32);
        Map<String, Long> pillarMap = new HashMap<>(14);

        List<Map<String, Object>> mrNumList =
                clusterYarnMapper.countMrTaskByDay(
                        yes7day, today, "dayTime,app_schedule_type", null, null, null, null, null);
        List<Map<String, Object>> sparkNumList =
                clusterYarnMapper.countSparkTaskByDay(
                        yes7day, today, "dayTime,app_schedule_type", null, null, null, null, null, null);

        if (mrNumList != null) {
            for (Iterator iterator = mrNumList.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String day = map.get("dayTime").toString();
                String type = map.get("app_schedule_type").toString();
                Long cnum = (Long) map.get("cnum");

                if ("调度".equals(type)) {
                    lineMap.put(day + "-1", cnum);
                } else {
                    lineMap.put(day + "-2", cnum);
                }
                Long x = lineMap.get(day + "-all");
                lineMap.put(day + "-all", cnum + (x == null ? 0 : x));

                pillarMap.put(day + "-mr", cnum + (x == null ? 0 : x));
            }
        }

        if (sparkNumList != null) {

            for (Iterator iterator = sparkNumList.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String day = map.get("dayTime").toString();
                String type = map.get("app_schedule_type").toString();
                Long cnum = (Long) map.get("cnum");

                Long old1 = lineMap.get(day + "-1");
                Long old2 = lineMap.get(day + "-2");
                Long oldall = lineMap.get(day + "-all");

                if ("调度".equals(type)) {
                    if (old1 != null) {
                        lineMap.put(day + "-1", old1 + cnum);
                    }
                } else {
                    if (old2 != null) {
                        lineMap.put(day + "-2", old2 + cnum);
                    }
                }
                if (oldall != null) {
                    lineMap.put(day + "-all", oldall + cnum);
                }

                Long x = pillarMap.get(day + "-sp");
                pillarMap.put(day + "-sp", cnum + (x == null ? 0 : x));
            }
        }

        Long[] ddList = new Long[7];
        Long[] fddList = new Long[7];
        Long[] allList = new Long[7];

        Long[] mrList = new Long[7];
        Long[] spList = new Long[7];

        // 首页块状图
        Long app_yes = lineMap.get(yesDay + "-all") == null ? 0l : lineMap.get(yesDay + "-all");
        Long app_yesyes =
                lineMap.get(yesyesDay + "-all") == null ? 0l : lineMap.get(yesyesDay + "-all");

        if (app_yes >= app_yesyes) {
            resMap.put("app_color", "green-soft");
        } else {
            resMap.put("app_color", "red-soft");
        }

        Double rate = app_yesyes == 0 ? 0 : ((app_yes * 1.00 - app_yesyes * 1.00) / app_yesyes * 1.00);
        resMap.put("app_rate", df.format(rate * 100) + "%");
        resMap.put("app_yesNum", app_yes.toString());
        resMap.put("app_yesyesNum", app_yesyes.toString());

        for (int i = 0; i < xList.length; i++) {
            String day = xList[i];
            if (lineMap.get(day + "-1") != null) {
                ddList[i] = lineMap.get(day + "-1");
            } else {
                ddList[i] = 0l;
            }
            if (lineMap.get(day + "-2") != null) {
                fddList[i] = lineMap.get(day + "-2");
            } else {
                fddList[i] = 0l;
            }
            if (lineMap.get(day + "-all") != null) {
                allList[i] = lineMap.get(day + "-all");
            } else {
                allList[i] = 0l;
            }

            if (pillarMap.get(day + "-mr") != null) {
                mrList[i] = pillarMap.get(day + "-mr");
            } else {
                mrList[i] = 0l;
            }

            if (pillarMap.get(day + "-sp") != null) {
                spList[i] = pillarMap.get(day + "-sp");
            } else {
                spList[i] = 0l;
            }
        }

        resMap.put("xData", xList); // x轴
        resMap.put("ddData", ddList); // 调度任务
        resMap.put("fddData", fddList); // 非调度任务
        resMap.put("allData", allList); // 所有任务

        resMap.put("mrData", mrList); // 柱状图-mr任务
        resMap.put("spData", spList); // 柱状图-spark任务
        String[] colorArr = {"#003366", "#4cabce"};
        resMap.put("colorList", colorArr);

        return resMap;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月4日 @Description:统计超长任务
     */
    @Override
    public Map<String, Object> getLongTimeTaskCount(
            String yes7day, String today, String yesDay, String yesyesDay, String[] xList) {

        Map<String, Object> res = new HashMap<>();
        // 超时告警临界值
        Map<String, Object> map6 = clusterYarnMapper.getThresholdByColumn("APP_OVERTIME_MONITOR");
        Integer maxTime = (Integer) map6.get("threshold_value");

        // map个数临界值
        Map map1 = clusterYarnMapper.getThresholdByColumn("MR_MAPTASK_NUMBER");
        Integer maxMap = (Integer) map1.get("threshold_value");

        // reduce个数临界值
        Map map2 = clusterYarnMapper.getThresholdByColumn("MR_REDUCETASK_NUMBER");
        Integer maxRedu = (Integer) map2.get("threshold_value");

        // 资源过多临界值
        Map map3 =
                clusterYarnMapper.getThresholdByColumn(
                        "SPARK_EXECUTOR_MEMOTRY"); // spark 调度任务Executor-Memory（G-告警）
        Map map4 =
                clusterYarnMapper.getThresholdByColumn("SPARK_CPUCORE_TOTAL"); // spark 调度任务CpuCore（告警）
        Map map5 =
                clusterYarnMapper.getThresholdByColumn("SPARK_MEMORY_TOTAL"); // spark 调度任务memory（G-告警）
        Integer maxExMem = (Integer) map3.get("threshold_value");
        Integer maxCpu = (Integer) map4.get("threshold_value");
        Integer maxTotalMem = (Integer) map5.get("threshold_value");

        // 超长任务
        Map<String, Long> longTimeMap = new HashMap<>();
        List<Map<String, Object>> queryLongTimeTaskReport =
                clusterYarnMapper.queryLongTimeTaskReport(yes7day, today);
        if (queryLongTimeTaskReport != null && queryLongTimeTaskReport.size() > 0) {
            for (Iterator iterator = queryLongTimeTaskReport.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String day = map.get("dayTime").toString();
                Long cnum = (Long) map.get("cnum");
                longTimeMap.put(day, cnum);
            }
        }
    /*List<Map<String, Object>> countMrTaskByDay = clusterYarnMapper.countMrTaskByDay(yes7day, today, "dayTime", maxTime*60, null, null, null, null);
    List<Map<String, Object>> countSparkTaskByDay = clusterYarnMapper.countSparkTaskByDay(yes7day, today, "dayTime", maxTime*60, null, null, null, null, null);
    if(countMrTaskByDay!=null && countMrTaskByDay.size()>0) {
    	for (Iterator iterator = countMrTaskByDay.iterator(); iterator.hasNext();) {
    		Map<String, Object> map = (Map<String, Object>) iterator.next();
    		String day = map.get("dayTime").toString();
    		Long cnum = (Long) map.get("cnum");
    		longTimeMap.put(day, cnum);
    	}
    }
    if(countSparkTaskByDay!=null) {
    	for (Iterator iterator = countSparkTaskByDay.iterator(); iterator.hasNext();) {
    		Map<String, Object> map = (Map<String, Object>) iterator.next();
    		String day = map.get("dayTime").toString();
    		Long cnum = (Long) map.get("cnum");
    		Long old = longTimeMap.get(day);
    		longTimeMap.put(day, cnum+(old==null?0l:old));
    	}
    }*/

        // task过多
        Map<String, Long> longTaskMap = new HashMap<>();
        List<Map<String, Object>> queryLongTaskReport =
                clusterYarnMapper.queryLongTaskReport(yes7day, today);
        if (queryLongTaskReport != null && queryLongTaskReport.size() > 0) {
            for (Iterator iterator = queryLongTaskReport.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String day = map.get("dayTime").toString();
                Long cnum = (Long) map.get("cnum");
                longTaskMap.put(day, cnum);
            }
        }
    /*List<Map<String, Object>> countMrTaskByDay2 = clusterYarnMapper.countMrTaskByDay(yes7day, today, "dayTime", null, null, null, maxMap, maxRedu);
    if(countMrTaskByDay2!=null && countMrTaskByDay2.size()>0) {
    	for (Iterator iterator = countMrTaskByDay2.iterator(); iterator.hasNext();) {
    		Map<String, Object> map = (Map<String, Object>) iterator.next();
    		String day = map.get("dayTime").toString();
    		Long cnum = (Long) map.get("cnum");
    		longTaskMap.put(day, cnum);
    	}
    }*/

        // 资源过多
        Map<String, Long> longResourceMap = new HashMap<>();
        List<Map<String, Object>> queryLongResourceReport =
                clusterYarnMapper.queryLongResourceReport(yes7day, today);
        if (queryLongResourceReport != null && queryLongResourceReport.size() > 0) {
            for (Iterator iterator = queryLongResourceReport.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String day = map.get("dayTime").toString();
                Long cnum = (Long) map.get("cnum");
                longResourceMap.put(day, cnum);
            }
        }
    /*List<Map<String, Object>> countSparkTaskByDay2 = clusterYarnMapper.countSparkTaskByDay(yes7day, today, "dayTime", null, null, null, maxExMem, maxTotalMem, maxCpu);
    if(countSparkTaskByDay2!=null && countSparkTaskByDay2.size()>0) {
    	for (Iterator iterator = countSparkTaskByDay2.iterator(); iterator.hasNext();) {
    		Map<String, Object> map = (Map<String, Object>) iterator.next();
    		String day = map.get("dayTime").toString();
    		Long cnum = (Long) map.get("cnum");
    		longResourceMap.put(day, cnum);
    	}
    }*/

        // 超长任务 longTimeMap
        Long[] longTimeList = new Long[7];
        Long yesLongTime = longTimeMap.get(yesDay) == null ? 0l : longTimeMap.get(yesDay);
        Long yesyesLongTime = longTimeMap.get(yesyesDay) == null ? 0l : longTimeMap.get(yesyesDay);
        if (yesLongTime > yesyesLongTime) {
            res.put("time_color", "red-soft");
        } else {
            res.put("time_color", "green-soft");
        }
        Double timeRate =
                yesyesLongTime == 0
                        ? 0
                        : ((yesLongTime * 1.00 - yesyesLongTime * 1.00) / yesyesLongTime * 1.00);
        res.put("time_rate", df.format(timeRate * 100) + "%");
        res.put("time_yes", yesLongTime);
        res.put("time_yesyes", yesyesLongTime);

        // task过多 longTaskMap
        Long[] longTaskList = new Long[7];
        Long yesLongTask = longTaskMap.get(yesDay) == null ? 0l : longTaskMap.get(yesDay);
        Long yesyesLongTask = longTaskMap.get(yesyesDay) == null ? 0l : longTaskMap.get(yesyesDay);
        if (yesLongTask > yesyesLongTask) {
            res.put("task_color", "red-soft");
        } else {
            res.put("task_color", "green-soft");
        }
        Double taskRate =
                yesyesLongTask == 0
                        ? 0
                        : ((yesLongTask * 1.00 - yesyesLongTask * 1.00) / yesyesLongTask * 1.00);
        res.put("task_rate", df.format(taskRate * 100) + "%");
        res.put("task_yes", yesLongTask);
        res.put("task_yesyes", yesyesLongTask);

        // 资源过多 longResourceMap
        Long[] longResourceList = new Long[7];
        Long yesLongResource = longResourceMap.get(yesDay) == null ? 0l : longResourceMap.get(yesDay);
        Long yesyesLongResource =
                longResourceMap.get(yesyesDay) == null ? 0l : longResourceMap.get(yesyesDay);
        if (yesLongResource > yesyesLongResource) {
            res.put("resource_color", "red-soft");
        } else {
            res.put("resource_color", "green-soft");
        }
        Double resourceRate =
                yesyesLongResource == 0
                        ? 0
                        : ((yesLongResource * 1.00 - yesyesLongResource * 1.00) / yesyesLongResource * 1.00);
        res.put("resource_rate", df.format(resourceRate * 100) + "%");
        res.put("resource_yes", yesLongResource);
        res.put("resource_yesyes", yesyesLongResource);

        for (int i = 0; i < xList.length; i++) {
            String date = xList[i];
            Long timeNum = longTimeMap.get(date);
            Long taskNum = longTaskMap.get(date);
            Long resourceNum = longResourceMap.get(date);

            longTimeList[i] = timeNum == null ? 0l : timeNum;
            longTaskList[i] = taskNum == null ? 0l : taskNum;
            longResourceList[i] = resourceNum == null ? 0l : resourceNum;
        }

        res.put("longTimeData", longTimeList);
        res.put("longTaskData", longTaskList);
        res.put("longResourceData", longResourceList);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月4日 @Description: 获取运行时长分布任务
     */
    @Override
    public Map<String, Object> getRunTimePai(
            String yes7day, String today, String yesDay, String yesyesDay, Integer type) {

        Map<String, Object> res = new HashMap<>();

        Integer[] fenbu = {0, 1800, 3600, 7200, 999999};
        String[] nameList = {"0~30分", "30~60分", "60~120分", "120分以上"};

        Long[] runTimeList = new Long[nameList.length];
        Long total = 0l;
        for (int i = 0; i < runTimeList.length; i++) {
            runTimeList[i] = 0l;
        }

        String appType = null;
        if (type == 1) {
            appType = "调度";
            res.put("titleStr", "调度任务");
        } else if (type == 2) {
            appType = "非调度";
            res.put("titleStr", "非调度任务");
        } else {
            res.put("titleStr", "所有任务");
        }

        // mr任务
        List<Map<String, Object>> queryMrListByTime =
                clusterYarnMapper.queryMrListByTime(yesDay, today, appType);
        for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
            Map<String, Object> mr = (Map<String, Object>) iterator.next();
            for (int i = 0; i < fenbu.length; i++) {
                if (i == fenbu.length - 1) {
                    break;
                }
                int start = fenbu[i];
                int end = fenbu[i + 1];
                Long exTime = (Long) mr.get("execute_time");
                if (exTime >= start && exTime < end) {
                    runTimeList[i] = runTimeList[i] + 1;
                    break;
                }
            }
        }

        // spark任务
        List<Map<String, Object>> querySparkListByTime =
                clusterYarnMapper.querySparkListByTime(yesDay, today, appType);
        for (Iterator iterator = querySparkListByTime.iterator(); iterator.hasNext(); ) {
            Map<String, Object> sp = (Map<String, Object>) iterator.next();
            for (int i = 0; i < fenbu.length; i++) {
                int start = 0;
                int end = 0;
                if (i == 0) {
                    start = 0;
                    end = fenbu[i];
                } else if (i == fenbu.length - 1) {
                    start = fenbu[i];
                    end = 999999;
                } else {
                    start = fenbu[i - 1];
                    end = fenbu[i];
                }

                Long exTime = (Long) sp.get("execute_time");
                if (exTime >= start && exTime < end) {
                    runTimeList[i] = runTimeList[i] + 1;
                    break;
                }
            }
        }

        // total =
        // (queryMrListByTime!=null?queryMrListByTime.size():0l)+(querySparkListByTime!=null?querySparkListByTime.size():0l);
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (int i = 0; i < runTimeList.length; i++) {
            Long l = runTimeList[i];
            Map<String, Object> map = new HashMap<>();
            map.put("value", l);
            map.put("name", nameList[i]);
            dataList.add(map);
        }

        res.put("runTimeData", dataList);
        res.put("nameData", nameList);

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月11日 @Description:获取HDFS统计信息
     */
    @Override
    public Map<String, Object> getHdfsReport(
            String yes7day, String today, String yesDay, String yesyesDay, String[] xList) {

        String url1 =
                "http://10.148.15.6:50070/jmx?get=Hadoop:service=NameNode,name=FSNamesystem::CapacityTotal";
        String url2 =
                "http://10.148.15.156:50070/jmx?get=Hadoop:service=NameNode,name=FSNamesystem::CapacityTotal";

        long mb = 1048576;
        long gb = 1073741824l;
        long tb = 1099511627776l;
        long pb = 1125899906842620l;

        Long totalHdfs = 0l;
        // 各组hdfs使用量
        List<List<Double>> dataList = new ArrayList<>();
        // hdfs总使用量
        List<Double> dataList2 = new ArrayList<>();
        Map<String, Object> res = new HashMap<>();

        // 接口获取
        try {
            // HDFS总量
            String executePost = HttpUtils.httpGet(url1);
            Map<String, Object> map = (Map<String, Object>) JSONObject.parse(executePost);
            if (map != null && map.get("beans") != null) {
                JSONArray ja = (JSONArray) map.get("beans");
                Map<String, Object> mpa = (Map<String, Object>) ja.get(0);
                totalHdfs = (Long) mpa.get("CapacityTotal");
            }
            double ptotalHdfs = totalHdfs * 1.00 / (pb * 1.00);
            res.put("hdfsTotal", df.format(ptotalHdfs) + "PB");

            // hdfs使用量
            List<Map<String, Object>> hdfsReport = clusterYarnMapper.getHdfsReport("'/'", yesDay, today);
            if (hdfsReport != null && hdfsReport.size() > 0) {

                Map<String, Object> umap = hdfsReport.get(0);
                BigDecimal bfs = (BigDecimal) umap.get("filesize");
                Long fs = bfs.longValue();

                Double useRate = totalHdfs == 0 ? 0.00 : (fs * 1.00 / totalHdfs * 1.00) * 100;
                res.put("hdfsUseRate", df.format(useRate) + "%");

                if (useRate >= 0 && useRate <= 50) {
                    res.put("hdfsUseColor", "green-soft");
                } else if (useRate > 50 && useRate < 80) {
                    res.put("hdfsUseColor", "yellow-gold");
                } else if (useRate > 80) {
                    res.put("hdfsUseColor", "red-soft");
                }
                res.put("hdfsUsed", fs.toString());
            } else {
                res.put("hdfsUseRate", "0.00%");
                res.put("hdfsUsed", 0.00);
                res.put("hdfsUseColor", "green-soft");
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                String executePost = HttpUtils.httpGet(url2);
                Map<String, Object> map = (Map<String, Object>) JSONObject.parse(executePost);
                if (map != null && map.get("beans") != null) {
                    JSONArray ja = (JSONArray) map.get("beans");
                    Map<String, Object> mpa = (Map<String, Object>) ja.get(0);
                    totalHdfs = (Long) mpa.get("CapacityTotal");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }

        // 获取组用户
        List<String> teamList = clusterYarnMapper.getHdfsTeam();
        if (teamList == null || teamList.size() <= 0) {
            // 没有数据
            res.put("hdfsPostData", new ArrayList<>());
            res.put("teamData", new ArrayList<>());
            res.put("hdfsUsedTotalData", new ArrayList<>());
            res.put("hdfsIncrRate", "0.00%");
            res.put("hdfsIncrColor", "green-soft");
        } else {
            String[] teamArr = new String[teamList.size()];
            String dirStr = "";

            for (Iterator iterator = teamList.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                String v = "/home/" + string;
                dirStr += "'" + v + "',";
            }
            dirStr += "'/'";

            // 查询各组总量数据
            Map<String, Long> dataMap = new HashMap<>();
            List<Map<String, Object>> hdfsReport =
                    clusterYarnMapper.getHdfsReport(dirStr, yes7day, today);
            if (hdfsReport != null && hdfsReport.size() > 0) {

                for (Iterator iterator = hdfsReport.iterator(); iterator.hasNext(); ) {
                    Map<String, Object> map2 = (Map<String, Object>) iterator.next();
                    String dt = map2.get("dt").toString();
                    String team = map2.get("dir").toString();
                    BigDecimal filesize = (BigDecimal) map2.get("filesize");
                    dataMap.put(dt + team, filesize.longValue());
                }
            }

            // 拼数据
            for (int j = 0; j < xList.length; j++) {
                int x = 0;
                String date = xList[j];
                Long totalUsed = dataMap.get(date + "/") == null ? 0l : dataMap.get(date + "/");
                double ttotalUsed = totalUsed * 1.00 / (tb * 1.00);
                dataList2.add((double) Math.round(ttotalUsed * 100) / 100);
                for (Iterator iterator = teamList.iterator(); iterator.hasNext(); ) {
                    String team = iterator.next().toString();
                    String key = date + "/home/" + team;
                    Long filesize = dataMap.get(key) == null ? 0l : dataMap.get(key);
                    double tfilesize = filesize * 1.00 / (gb * 1.00);
                    List<Double> list = null;
                    if (dataList.size() >= x + 1) {
                        list = dataList.get(x);
                    }

                    if (list == null) {
                        list = new ArrayList<>();
                    }

                    list.add((double) Math.round(tfilesize * 100) / 100);
                    if (j == 0) {
                        dataList.add(list);
                    }
                    x++;
                }
            }

            res.put("hdfsPostData", dataList);
            res.put("hdfsUsedTotalData", dataList2);
            res.put("teamData", teamList);

            // 日增量
            // 前天
            long yesyesData = dataMap.get(yesyesDay + "/") == null ? 0l : dataMap.get(yesyesDay + "/");
            // 昨天
            long yesData = dataMap.get(yesDay + "/") == null ? 0l : dataMap.get(yesDay + "/");
            // 昨日增量
            long incrData = yesData - yesyesData;
            // 昨日增长比
            Double incrRate = yesyesData == 0 ? 0.00 : (incrData * 1.00 / yesyesData * 1.00) * 100.00;
            res.put("hdfsIncrRate", df.format(incrRate) + "%");
            Double gincrData = (incrData * 1.00) / (gb * 1.00);
            res.put("hdfsIncrData", df.format(gincrData) + "GB");
        }

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月13日 @Description:获取小文件增长趋势
     */
    @Override
    public Map<String, Object> getHdfsLittleDirReport(
            String yes7day,
            String today,
            String yesDay,
            String yesyesDay,
            String[] xList,
            Integer dataType) {
        Map<String, Object> res = new HashMap<>();
        String yes8day = DateUtil.getNDaysAgo(8, "yyyy-MM-dd");
        String dirStr = "'/'";
        List<String> teamList = new ArrayList<>();
        // 各组hdfs使用量
        List<List<Long>> dataList = new ArrayList<>();

        if (dataType == 0) {
            // 小文件增长趋势

            teamList.add("全部");

        } else if (dataType == 1) {
            // 各组小文件增长趋势
            // 获取组用户
            teamList = clusterYarnMapper.getHdfsTeam();
            if (teamList != null && teamList.size() > 0) {
                for (Iterator iterator = teamList.iterator(); iterator.hasNext(); ) {
                    String string = (String) iterator.next();
                    dirStr += ",'/home/" + string + "'";
                }
            } else {
                // 空了
                res.put("dataList", new ArrayList<>());
                res.put("teamList", new ArrayList<>());
                return res;
            }
        }

        List<Map<String, Object>> hdfsLittleReport =
                clusterYarnMapper.getHdfsLittleReport(dirStr, yes8day, today);
        Map<String, Long> littMap = new HashMap<>();
        if (hdfsLittleReport != null && hdfsLittleReport.size() > 0) {
            for (Iterator iterator = hdfsLittleReport.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String dt = map.get("dt").toString();
                String team = map.get("dir").toString();
                BigDecimal filesize = (BigDecimal) map.get("little_num");
                BigDecimal file_num = (BigDecimal) map.get("file_num");

                littMap.put(dt + team, filesize.longValue());
                littMap.put(dt + team + "all", file_num.longValue());
            }
        }

        if (dataType == 0) {
            // 全部小文件
            List<Long> liList = new ArrayList<>();
            for (int j = 0; j < xList.length; j++) {
                String date = xList[j];
                String lastDate = "";
                try {
                    lastDate = DateUtil.getNDaysAgo(date, "yyyy-MM-dd", 1, "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String key = date + "/";
                String lastKey = lastDate + "/";

                Long filesize = littMap.get(key) == null ? 0l : littMap.get(key);
                Long lastFilesize = littMap.get(lastKey) == null ? 0l : littMap.get(lastKey);

                liList.add(filesize - lastFilesize);
            }
            dataList.add(liList);

        } else if (dataType == 1) {
            // 各组小文件
            // 拼数据
            for (int j = 0; j < xList.length; j++) {
                int x = 0;
                String date = xList[j];
                String lastDate = "";
                try {
                    lastDate = DateUtil.getNDaysAgo(date, "yyyy-MM-dd", 1, "yyyy-MM-dd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                for (Iterator iterator = teamList.iterator(); iterator.hasNext(); ) {
                    String team = iterator.next().toString();
                    String key = date + "/home/" + team;
                    String lastKey = lastDate + "/home/" + team;

                    Long filesize = littMap.get(key) == null ? 0l : littMap.get(key);
                    Long lastFilesize = littMap.get(lastKey) == null ? 0l : littMap.get(lastKey);

                    List<Long> list = null;
                    if (dataList.size() >= x + 1) {
                        list = dataList.get(x);
                    }

                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(filesize - lastFilesize);
                    if (j == 0) {
                        dataList.add(list);
                    }
                    x++;
                }
            }
        }

        res.put("littleDataList", dataList);
        res.put("teamData", teamList);

        // 小文件box
        Long allDir = littMap.get(yesDay + "/all") == null ? 0l : littMap.get(yesDay + "/all");
        Long littDir = littMap.get(yesDay + "/") == null ? 0l : littMap.get(yesDay + "/");

        Double littRate = allDir == 0 ? 0.00 : (littDir * 1.00 / allDir * 1.00) * 100.00;
        res.put("littRate", df.format(littRate) + "%");
        res.put("yesLittleData", littDir);

        if (littRate >= 0 && littRate <= 50) {
            res.put("littDirColor", "green-soft");
        } else if (littRate > 50 && littRate < 80) {
            res.put("littDirColor", "yellow-gold");
        } else if (littRate > 80) {
            res.put("littDirColor", "red-soft");
        }

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月17日 @Description:获取冷数据列表
     */
    @Override
    public Map<String, Object> getColdDataList(Pageable pageable, Integer isDel, String coldDir) {

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> queryColdDataList =
                clusterYarnMapper.queryColdDataList(
                        isDel, coldDir, null, pageable.getOffset(), pageable.getPageSize());
        if (queryColdDataList != null) {
            for (Iterator iterator = queryColdDataList.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Timestamp day = (Timestamp) map.get("atime");
                map.put("atimeStr", DateUtil.format2.format(day));
            }
        } else {
            queryColdDataList = new ArrayList<>();
        }

        Long countColdDataList = clusterYarnMapper.countColdDataList(isDel, coldDir, null);
        resultMap.put("recordsFiltered", countColdDataList); // 返回搜索条件查询结果数
        resultMap.put("data", queryColdDataList);
        resultMap.put("recordsTotal", countColdDataList); // 返回没有任何过滤条件的总数

        return resultMap;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月17日 @Description:查询删除中的数据有多少
     */
    @Override
    public Long countColdDataByStatus(Integer isDel, String ids) {

        Long countColdDataList = clusterYarnMapper.countColdDataList(isDel, null, ids);
        return countColdDataList;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月17日 @Description:通过任务拉取冷数据
     */
  /*	@Override
  public Map<String, Object> getColdDataByTask(String date) {
  	// TODO Auto-generated method stub
  	return null;
  }*/

    /**
     * @author wangkaixuan
     * @date 2018年12月18日 @Description:查询冷数据管理任务的拉取任务状态
     */
    @Override
    public Integer getColdDataTaskStatus(Integer jobId) {
        Integer coldDataTaskStatus = clusterYarnMapper.getColdDataTaskStatus(jobId);
        return coldDataTaskStatus;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月18日 @Description:根据ids获取冷数据列表
     */
    @Override
    public List<Map<String, Object>> getDelColdDataList(String ids) {
        List<Map<String, Object>> queryColdDataList = null;
        if (StringUtils.isNotBlank(ids)) {
            queryColdDataList = clusterYarnMapper.queryColdDataList(null, null, ids, null, null);
        }

        return queryColdDataList;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月18日 @Description:根据ids批量更新数据状态
     */
    @Override
    public void updateColdData(String ids, Integer status) {
        clusterYarnMapper.updateColdDataById(status, "", ids);
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月19日 @Description: 获取hdfs目录文件数列表
     */
    @Override
    public Map<String, Object> getHdfsFileNumData(Integer menuType) {

        Map<String, Object> resultMap = new HashMap<>();
        String yesDay = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String yes7day = DateUtil.getNDaysAgo(7, "yyyy-MM-dd");
        String yes8day = DateUtil.getNDaysAgo(8, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        List<List<Long>> teamTotalData = new ArrayList<>();
        List<List<Long>> teamIncreData = new ArrayList<>();

        Map<String, String> dateMap = new HashMap<>();
        Set<String> dashDateRangeSet = null;
        try {
            dashDateRangeSet = DateUtil.getDashDateRangeSet(yes8day, yesDay);
            String lastDate = "";
            for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                if (!lastDate.equals("")) {
                    dateMap.put(string, lastDate);
                }
                lastDate = string;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        List<String> teamList = clusterYarnMapper.getHdfsTeam();
        String dirStr = "";
        if (teamList != null && teamList.size() > 0) {
            for (Iterator iterator = teamList.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                dirStr += "'/home/" + string + "',";
            }
        } else {
            // 空了
            dashDateRangeSet.remove(yes8day);
            resultMap.put("data", new ArrayList<>());
            resultMap.put("xData", dashDateRangeSet);
            resultMap.put("teamData", new ArrayList<>());
            resultMap.put("yNumData", new ArrayList<>());
            resultMap.put("yIncreData", new ArrayList<>());
            return resultMap;
        }

        dirStr = dirStr.substring(0, dirStr.lastIndexOf(","));
        Map<String, Long> hdfsMap = new HashMap<>();
        // 获取全部数据
        List<Map<String, Object>> allHdfsFileNumList = null;
        if (menuType == 1) {
            // 文件数信息
            allHdfsFileNumList = clusterYarnMapper.getHdfsFileNumList(dirStr, yes8day, today, null, null);
        } else if (menuType == 2) {
            // 小文件数信息
            allHdfsFileNumList =
                    clusterYarnMapper.getHdfsLittleFileNumList(dirStr, yes8day, today, null, null);
        }

        if (allHdfsFileNumList != null && allHdfsFileNumList.size() > 0) {
            int index = allHdfsFileNumList.size() - 1;
            for (; index >= 0; index--) {
                Map<String, Object> map = allHdfsFileNumList.get(index);
                String dt = map.get("dt").toString();
                String dir = map.get("dir").toString();
                BigDecimal bnum = (BigDecimal) map.get("fileNum");
                Long num = bnum.longValue();
                hdfsMap.put(dt + dir, num);
                if (dt.equals(yes8day)) {
                    allHdfsFileNumList.remove(index);
                } else {
                    // 获取昨天的日期
                    String yes = dateMap.get(dt);
                    Long yesNum = hdfsMap.get(yes + dir) == null ? 0l : hdfsMap.get(yes + dir);
                    long incre = num - yesNum;
                    map.put("incre", incre);
                    // 存放每组的增量信息
                    hdfsMap.put("i" + dt + dir, incre);
                }
            }
        } else {
            allHdfsFileNumList = new ArrayList<>();
      /*dashDateRangeSet.remove(yes8day);
      resultMap.put("data", new ArrayList<>());
      resultMap.put("xData", dashDateRangeSet);
      resultMap.put("teamData", new ArrayList<>());
      resultMap.put("yNumData", new ArrayList<>());
      resultMap.put("yIncreData", new ArrayList<>());
      return resultMap;*/
        }

        // 拼装图表数据
        int tindex = 0;
        dashDateRangeSet.remove(yes8day);

        for (Iterator iterator = teamList.iterator(); iterator.hasNext(); ) {
            String team = (String) iterator.next();
            List<Long> numList = new ArrayList<>();
            List<Long> increList = new ArrayList<>();
            for (Iterator iterator2 = dashDateRangeSet.iterator(); iterator2.hasNext(); ) {
                String date = (String) iterator2.next();

                String numKey = "" + date + "/home/" + team;
                String incKey = "i" + numKey;

                Long fnum = hdfsMap.get(numKey) == null ? 0l : hdfsMap.get(numKey);
                Long inum = hdfsMap.get(incKey) == null ? 0l : hdfsMap.get(incKey);
                numList.add(fnum);
                increList.add(inum);
            }
            teamTotalData.add(numList);
            teamIncreData.add(increList);
            tindex++;
        }

    /*List<Map<String, Object>> hdfsFileNumList = clusterYarnMapper.getHdfsFileNumList(dirStr, yes8day, today, pageable.getOffset(), pageable.getPageSize());
    if(hdfsFileNumList!=null && hdfsFileNumList.size()>0) {
    	for (Iterator iterator = hdfsFileNumList.iterator(); iterator.hasNext();) {
    		Map<String, Object> map = (Map<String, Object>) iterator.next();


    	}

    }else {
    	resultMap.put("recordsFiltered", 0); // 返回搜索条件查询结果数
    	resultMap.put("data", new ArrayList<>());
    	resultMap.put("recordsTotal", 0); // 返回没有任何过滤条件的总数
    	return resultMap;
    }
    */
        // List<Map<String, Object>> countHdfsFileNum = clusterYarnMapper.countHdfsFileNum(dirStr,
        // yes7day, today);

        // resultMap.put("recordsFiltered", countHdfsFileNum); // 返回搜索条件查询结果数

        // resultMap.put("recordsTotal", countHdfsFileNum); // 返回没有任何过滤条件的总数

        // 图表数据
        resultMap.put("data", allHdfsFileNumList);
        resultMap.put("xData", dashDateRangeSet);
        resultMap.put("teamData", teamList);
        resultMap.put("yNumData", teamTotalData);
        resultMap.put("yIncreData", teamIncreData);
        return resultMap;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月19日 @Description:获取目录下子目录文件数
     */
    @Override
    public Map<String, Object> getHdfsFileDetailNumData(
            String dirStr, String date, Integer menuType) {

        Map<String, Object> resMap = new HashMap<>();
        // String[] subDirArr =
        // {"/home/zdp/middata","/home/zdp/rawdata","/home/zdp/resultdata","/home/zdp/warehouse"};
        String[] subDirNameArr = {"middata", "rawdata", "resultdata", "warehouse"};
        String tom1day = DateUtil.getNDayAgoOrBeforeByDate(date, "yyyy-MM-dd", 1);
        String yes8day = DateUtil.getNDayAgoOrBeforeByDate(date, "yyyy-MM-dd", -7);
        String yes7day = DateUtil.getNDayAgoOrBeforeByDate(date, "yyyy-MM-dd", -6);

        List<List<Long>> teamIncreData = new ArrayList<>();

        Map<String, String> dateMap = new HashMap<>();
        Set<String> dashDateRangeSet = null;
        try {
            dashDateRangeSet = DateUtil.getDashDateRangeSet(yes8day, date);
            String lastDate = "";
            for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                if (!lastDate.equals("")) {
                    dateMap.put(string, lastDate);
                }
                lastDate = string;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, Long> hdfsMap = new HashMap<>();

        List<Map<String, Object>> hdfsSubFileNumList = null;

        if (menuType == 1) {
            // 文件数信息
            hdfsSubFileNumList =
                    clusterYarnMapper.getHdfsSubFileNumList("'" + dirStr + "'", yes8day, tom1day, null);
        } else if (menuType == 2) {
            // 小文件数信息
            hdfsSubFileNumList =
                    clusterYarnMapper.getHdfsLittleSubFileNumList("'" + dirStr + "'", yes8day, tom1day, null);
        }

        if (hdfsSubFileNumList != null && hdfsSubFileNumList.size() > 0) {
            int index = hdfsSubFileNumList.size() - 1;
            for (; index >= 0; index--) {
                Map<String, Object> map = hdfsSubFileNumList.get(index);
                String dt = map.get("dt").toString();
                String dir = map.get("sub_dir").toString();
                BigDecimal bnum = (BigDecimal) map.get("fileNum");
                Long num = bnum.longValue();
                hdfsMap.put(dt + dir, num);
                if (dt.equals(yes8day)) {
                    hdfsSubFileNumList.remove(index);
                } else {
                    // 获取昨天的日期
                    String yes = dateMap.get(dt);
                    Long yesNum = hdfsMap.get(yes + dir) == null ? 0l : hdfsMap.get(yes + dir);
                    long incre = num - yesNum;
                    map.put("incre", incre);
                    // 存放每组的增量信息
                    hdfsMap.put("i" + dt + dir, incre);
                }
            }
        } else {
            // 没有数据
            hdfsSubFileNumList = new ArrayList<>();
        }

        // 查询总量
        Map<String, Long> totalMap = new HashMap<>();
        List<Map<String, Object>> allHdfsFileNumList = null;
        if (menuType == 1) {
            // 文件数信息
            allHdfsFileNumList =
                    clusterYarnMapper.getHdfsFileNumList("'" + dirStr + "'", yes8day, tom1day, null, null);
        } else if (menuType == 2) {
            // 小文件数信息
            allHdfsFileNumList =
                    clusterYarnMapper.getHdfsLittleFileNumList(
                            "'" + dirStr + "'", yes8day, tom1day, null, null);
        }
        if (allHdfsFileNumList != null && allHdfsFileNumList.size() > 0) {
            int index = allHdfsFileNumList.size() - 1;
            for (; index >= 0; index--) {
                Map<String, Object> map = allHdfsFileNumList.get(index);
                String dt = map.get("dt").toString();
                String dir = map.get("dir").toString();
                BigDecimal bnum = (BigDecimal) map.get("fileNum");
                Long num = bnum.longValue();
                totalMap.put(dt + dir, num);
                if (dt.equals(yes8day)) {
                    allHdfsFileNumList.remove(index);
                } else {
                    // 获取昨天的日期
                    String yes = dateMap.get(dt);
                    Long yesNum = totalMap.get(yes + dir) == null ? 0l : totalMap.get(yes + dir);
                    long incre = num - yesNum;
                    // map.put("incre", incre);
                    // 存放每组的增量信息
                    totalMap.put("i" + dt + dir, incre);
                    totalMap.put(dt + dir, num);
                }
            }
        }

        List<Map<String, Object>> dataList = new ArrayList<>();
        // 拼装表格数据
        dashDateRangeSet.remove(yes8day);
        for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
            String day = (String) iterator.next();
            Map<String, Object> map = new HashMap<>();
            map.put("dt", day);
            map.put("dir", dirStr);
            // long totalNum = 0l;
            // long totalIncr = 0l;
            for (int i = 0; i < subDirNameArr.length; i++) {
                String subDirStr = dirStr + "/" + subDirNameArr[i];
                String theKey = day + subDirStr;
                String incKey = "i" + theKey;
                map.put("sub_dir", subDirStr);
                Long num = hdfsMap.get(theKey) == null ? 0l : hdfsMap.get(theKey);
                Long incNum = hdfsMap.get(incKey) == null ? 0l : hdfsMap.get(incKey);
                map.put("num" + i, num);
                map.put("incNum" + i, incNum);
                // totalNum += num;
                // totalIncr += incNum;
            }
            String key = day + dirStr;
            String ikey = "i" + day + dirStr;
            map.put("totalNum", totalMap.get(key) == null ? 0 : totalMap.get(key));
            map.put("totalIncr", totalMap.get(ikey) == null ? 0 : totalMap.get(ikey));
            dataList.add(map);
        }

        // 拼装图表数据
        for (int i = 0; i < subDirNameArr.length; i++) {
            String subName = dirStr + "/" + subDirNameArr[i];
            List<Long> increList = new ArrayList<>();
            for (Iterator iterator2 = dashDateRangeSet.iterator(); iterator2.hasNext(); ) {
                String theDay = (String) iterator2.next();
                String incKey = "i" + theDay + subName;

                Long inum = hdfsMap.get(incKey) == null ? 0l : hdfsMap.get(incKey);
                increList.add(inum);
            }
            teamIncreData.add(increList);
        }

        resMap.put("yData", teamIncreData);
        resMap.put("xData", dashDateRangeSet);
        resMap.put("subDirData", subDirNameArr);
        resMap.put("tableData", dataList);

        return resMap;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:查询小文件目录列表
     */
    @Override
    public List<Map<String, Object>> getLittleDirList(Integer isMerge, String dirStr, String ids) {

        if (StringUtils.isBlank(dirStr)) {
            dirStr = null;
        }
        List<Map<String, Object>> littleDirList =
                clusterYarnMapper.getLittleDirList(dirStr, isMerge, ids);
        if (littleDirList != null && littleDirList.size() > 0) {

            for (Iterator iterator = littleDirList.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Long littNum = (Long) map.get("num");
                Long fileNum = (Long) map.get("file_num");
                Integer merge = (Integer) map.get("is_merge");
                Double rate = fileNum == 0 ? 0.00 : littNum * 1.00 / fileNum * 1.00;
                map.put("fileRate", df.format(rate * 100) + "%");
                String mergeStr = "";
                if (merge == 0) {
                    mergeStr = "未合并";
                } else if (merge == 1) {
                    mergeStr = "合并中";
                } else if (merge == 2) {
                    mergeStr = "合并成功";
                } else if (merge == 3) {
                    mergeStr = "合并失败";
                }
                map.put("mergeStr", mergeStr);
            }
        }

        return littleDirList;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:查询小文件数量
     */
    @Override
    public Long countLittleDirList(String isMerges, String dirStr, String ids) {
        Long countLittleDirList = clusterYarnMapper.countLittleDirList(dirStr, isMerges, ids);
        return countLittleDirList;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:更新小文件状态
     */
    @Override
    public void updateLittleDir(String ids, Integer isMerge) {
        clusterYarnMapper.updateLittleDirById(isMerge, "", ids);
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月24日 @Description:保存小文件目录
     */
    @Override
    public ClusterHdfsMerge saveClusterHdfsMerge(ClusterHdfsMerge clusterHdfsMerge) {
        clusterYarnMapper.insertLittleDir(clusterHdfsMerge);
        return clusterHdfsMerge;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月2日 @Description:获取各队列任务个数饼图数据
     */
    @Override
    public Map<String, Object> getTaskPieReportData() {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        String yes = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");

        List<Map<String, Object>> countMrTaskByDay =
                clusterYarnMapper.countMrTaskByDay(yes, today, "queue_name", null, null, null, null, null);
        List<Map<String, Object>> countSparkTaskByDay =
                clusterYarnMapper.countSparkTaskByDay(
                        yes, today, "queue_name", null, null, null, null, null, null);
        // List<String> nameList = new ArrayList<>();

        Map<String, Long> dataMap = new HashMap<>();
        if (countMrTaskByDay != null && countMrTaskByDay.size() > 0) {
            for (Iterator iterator = countMrTaskByDay.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String que = map.get("queue_name") == null ? "" : map.get("queue_name").toString();
                Long cnum = map.get("cnum") == null ? 0l : (Long) map.get("cnum");
                dataMap.put(que, cnum);
                // nameList.add(que);
            }
        }

        if (countSparkTaskByDay != null && countSparkTaskByDay.size() > 0) {
            for (Iterator iterator = countSparkTaskByDay.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String que = map.get("queue_name") == null ? "" : map.get("queue_name").toString();
                Long cnum = map.get("cnum") == null ? 0l : (Long) map.get("cnum");
                if (dataMap.get(que) == null) {
                    dataMap.put(que, cnum);
                    // nameList.add(que);
                } else {
                    Long cnum2 = dataMap.get(que);
                    dataMap.put(que, cnum + cnum2);
                }
            }
        }

        // 拼装数据
        List<String> nameList = new ArrayList<>();
        for (Iterator iterator = dataMap.entrySet().iterator(); iterator.hasNext(); ) {
            Entry en = (Entry) iterator.next();
            String key = en.getKey().toString();
            Long value = (Long) en.getValue();
            nameList.add(key);
            Map<String, Object> map = new HashMap<>();
            map.put("value", value);
            map.put("name", key);
            // map.put(key, value);
            dataList.add(map);
        }

        res.put("queueTaskData", dataList);
        res.put("queueNameData", nameList);

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月2日 @Description:获取24小时各小时的任务数统计
     */
    @Override
    public Map<String, Object> get24HoursTaskReportData(Integer reprotType, String reportDate) {
        Map<String, Object> res = new HashMap<>();
        String yes = "";
        String today = "";
        try {
            yes = DateUtil.getNDaysAgo(reportDate, "yyyy-MM-dd", 1, "yyyy-MM-dd");
            today = reportDate;
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        String[] hourArr = {
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23", "24"
        };
        String[] hourArr2 = {
                "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h", "11h", "12h", "13h", "14h",
                "15h", "16h", "17h", "18h", "19h", "20h", "21h", "22h", "23h", "24h"
        };

        Long[] dataList = new Long[24];
        Long[] dataList2 = new Long[24];
        for (int i = 0; i < hourArr.length - 1; i++) {
            dataList[i] = 0l;
            dataList2[i] = 0l;
        }

        List<Long> timeList = new ArrayList<>();
        for (int i = 0; i <= hourArr.length - 1; i++) {
            try {
                Long time = 0l;
                if (i == hourArr.length - 1) {
                    time = DateUtil.format3.parse(today + " " + "00:00:00").getTime();
                } else {
                    time = DateUtil.format3.parse(yes + " " + hourArr[i] + ":00:00").getTime();
                }
                timeList.add(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    /*Long endTime = 0l;
    try {
    	endTime = DateUtil.format3.parse(today+" "+"00:00:00").getTime();
    } catch (ParseException e) {
    	e.printStackTrace();
    }*/

        // mr任务
        List<Map<String, Object>> queryMrListByTime =
                clusterYarnMapper.queryMrListByTime(yes, today, null);
        // spark任务
        List<Map<String, Object>> querySparkListByTime =
                clusterYarnMapper.querySparkListByTime(yes, today, null);

        if (reprotType == null || reprotType == 0) {
            // 全部任务统计
            queryMrListByTime.addAll(querySparkListByTime);
            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Date time = (Date) map.get("finish_time");
                Long ttime = time.getTime();
                for (int j = 0; j < timeList.size() - 1; j++) {
                    long stime = timeList.get(j);
                    long etime = timeList.get(j + 1);
                    if (ttime >= stime && ttime < etime) {
                        Long v = dataList[j];
                        v += 1;
                        dataList[j] = v;
                        break;
                    }
                }
            }

        } else if (reprotType == 1) {
            // 任务类型
            String[] nameStr = {"MR", "SPARK"};

            // mr任务
            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Date time = (Date) map.get("finish_time");
                Long ttime = time.getTime();
                for (int j = 0; j < timeList.size() - 1; j++) {
                    long stime = timeList.get(j);
                    long etime = timeList.get(j + 1);
                    if (ttime >= stime && ttime < etime) {
                        Long v = dataList[j];
                        v += 1;
                        dataList[j] = v;
                        break;
                    }
                }
            }

            // spark任务
            for (Iterator iterator = querySparkListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Date time = (Date) map.get("finish_time");
                Long ttime = time.getTime();
                for (int j = 0; j < timeList.size() - 1; j++) {
                    long stime = timeList.get(j);
                    long etime = timeList.get(j + 1);
                    if (ttime >= stime && ttime < etime) {
                        Long v = dataList2[j];
                        v += 1;
                        dataList2[j] = v;
                        break;
                    }
                }
            }
            res.put("taskTypeData", nameStr);
        } else if (reprotType == 2) {
            // 运行类型
            String[] nameStr = {"调度任务", "非调度任务"};
            queryMrListByTime.addAll(querySparkListByTime);
            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Date time = (Date) map.get("finish_time");
                String app_schedule_type = map.get("app_schedule_type").toString();
                Long ttime = time.getTime();
                for (int j = 0; j < timeList.size() - 1; j++) {
                    long stime = timeList.get(j);
                    long etime = timeList.get(j + 1);
                    if (ttime >= stime && ttime < etime) {
                        if (app_schedule_type.equals("调度")) {
                            Long v = dataList[j];
                            v += 1;
                            dataList[j] = v;
                        } else if (app_schedule_type.equals("非调度")) {
                            Long v = dataList2[j];
                            v += 1;
                            dataList2[j] = v;
                        }
                        break;
                    }
                }
            }
            res.put("runTypeData", nameStr);
        }

        res.put("dataList", dataList);
        res.put("dataList2", dataList2);
        res.put("xData", hourArr2);

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月2日 @Description:获取各部门job数占比
     */
    @Override
    public Map<String, Object> getDeptJobReprotData(Integer reportType) {
        Map<String, Object> res = new HashMap<>();
        String yes = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        Map<String, Long> dataMap = new HashMap<>();
        if (reportType == 0) {
            // 全部
            // mr任务
            List<Map<String, Object>> queryMrListByTime =
                    clusterYarnMapper.queryMrListByTime(yes, today, null);
            // spark任务
            List<Map<String, Object>> querySparkListByTime =
                    clusterYarnMapper.querySparkListByTime(yes, today, null);
            queryMrListByTime.addAll(querySparkListByTime);

            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String usr = map.get("user_name").toString();

                if (dataMap.get(usr) == null) {
                    dataMap.put(usr, 1l);
                } else {
                    Long num = dataMap.get(usr);
                    dataMap.put(usr, num + 1);
                }
            }
            res.put("title", "全部任务");
        } else if (reportType == 1) {
            // MR任务
            List<Map<String, Object>> queryMrListByTime =
                    clusterYarnMapper.queryMrListByTime(yes, today, null);
            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String usr = map.get("user_name").toString();

                if (dataMap.get(usr) == null) {
                    dataMap.put(usr, 1l);
                } else {
                    Long num = dataMap.get(usr);
                    dataMap.put(usr, num + 1);
                }
            }
            res.put("title", "MR任务");
        } else if (reportType == 2) {
            // spark任务
            List<Map<String, Object>> querySparkListByTime =
                    clusterYarnMapper.querySparkListByTime(yes, today, null);
            for (Iterator iterator = querySparkListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String usr = map.get("user_name").toString();

                if (dataMap.get(usr) == null) {
                    dataMap.put(usr, 1l);
                } else {
                    Long num = dataMap.get(usr);
                    dataMap.put(usr, num + 1);
                }
            }
            res.put("title", "SPARK任务");
        } else if (reportType == 3) {
            // 调度任务
            List<Map<String, Object>> queryMrListByTime =
                    clusterYarnMapper.queryMrListByTime(yes, today, "调度");
            // spark任务
            List<Map<String, Object>> querySparkListByTime =
                    clusterYarnMapper.querySparkListByTime(yes, today, "调度");
            queryMrListByTime.addAll(querySparkListByTime);
            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String usr = map.get("user_name").toString();

                if (dataMap.get(usr) == null) {
                    dataMap.put(usr, 1l);
                } else {
                    Long num = dataMap.get(usr);
                    dataMap.put(usr, num + 1);
                }
            }
            res.put("title", "调度任务");
        } else if (reportType == 4) {
            // 非调度任务
            List<Map<String, Object>> queryMrListByTime =
                    clusterYarnMapper.queryMrListByTime(yes, today, "非调度");
            // spark任务
            List<Map<String, Object>> querySparkListByTime =
                    clusterYarnMapper.querySparkListByTime(yes, today, "非调度");
            queryMrListByTime.addAll(querySparkListByTime);
            for (Iterator iterator = queryMrListByTime.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                String usr = map.get("user_name").toString();

                if (dataMap.get(usr) == null) {
                    dataMap.put(usr, 1l);
                } else {
                    Long num = dataMap.get(usr);
                    dataMap.put(usr, num + 1);
                }
            }
            res.put("title", "非调度任务");
        }

        // 拼装数据
        List<String> nameList = new ArrayList<>();
        List<Map<String, Object>> dataList = new ArrayList<>();
        for (Iterator iterator = dataMap.entrySet().iterator(); iterator.hasNext(); ) {
            Entry en = (Entry) iterator.next();
            String key = en.getKey().toString();
            Long value = (Long) en.getValue();
            nameList.add(key);
            Map<String, Object> map = new HashMap<>();
            map.put("value", value);
            map.put("name", key);
            // map.put(key, value);
            dataList.add(map);
        }
        res.put("deptTaskData", dataList);
        res.put("deptNameData", nameList);

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:查询flume任务管理列表
     */
    @Override
    public Map<String, Object> getFlumeManageList(Pageable pageable, String urlStr, String rangeStr) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> queryColdDataList =
                clusterYarnMapper.queryFlumeManageList(
                        urlStr, rangeStr, pageable.getOffset(), pageable.getPageSize());
        if (queryColdDataList != null) {
            for (Iterator iterator = queryColdDataList.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Timestamp day = (Timestamp) map.get("create_time");
                map.put("createTime", DateUtil.format2.format(day));
            }
        } else {
            queryColdDataList = new ArrayList<>();
        }

        Long countColdDataList = clusterYarnMapper.countFlumeManageList(urlStr, rangeStr);
        resultMap.put("recordsFiltered", countColdDataList); // 返回搜索条件查询结果数
        resultMap.put("data", queryColdDataList);
        resultMap.put("recordsTotal", countColdDataList); // 返回没有任何过滤条件的总数
        return resultMap;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:保存或更新flume管理数据
     */
    @Override
    public Map<String, Object> saveOrUpdateFlumeManage(
            Integer id, String urlStr, String rangeStr, String crUser) {

        if (id == null) {
            clusterYarnMapper.insertFlumeManage(urlStr, rangeStr, crUser);
        } else {
            clusterYarnMapper.updateFlumeManage(urlStr, rangeStr, crUser, id);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("errorCode", 0);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:删除flume管理数据
     */
    @Override
    public Map<String, Object> deleteFlumeManage(Integer id) {
        clusterYarnMapper.deleteFlumeManage(id);
        Map<String, Object> res = new HashMap<>();
        res.put("errorCode", 0);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:获取flume数据详情
     */
    @Override
    public Map<String, Object> getFlumeManageDetail(Integer id) {
        Map<String, Object> flumeManageDetail = clusterYarnMapper.getFlumeManageDetail(id);
        return flumeManageDetail;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:获取stream任务告警列表
     */
    @Override
    public Map<String, Object> getStreamAlarmList(
            Pageable pageable,
            String taskId,
            String groupId,
            String project,
            String developUser,
            String rangeStr) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> queryColdDataList =
                clusterYarnMapper.queryStreamAlarmList(
                        taskId,
                        groupId,
                        project,
                        developUser,
                        rangeStr,
                        pageable.getOffset(),
                        pageable.getPageSize());
        if (queryColdDataList != null) {
            for (Iterator iterator = queryColdDataList.iterator(); iterator.hasNext(); ) {
                Map<String, Object> map = (Map<String, Object>) iterator.next();
                Timestamp day = (Timestamp) map.get("create_time");
                map.put("createTime", DateUtil.format2.format(day));
            }
        } else {
            queryColdDataList = new ArrayList<>();
        }

        Long countColdDataList =
                clusterYarnMapper.countStreamAlarmList(taskId, groupId, project, developUser, rangeStr);
        resultMap.put("recordsFiltered", countColdDataList); // 返回搜索条件查询结果数
        resultMap.put("data", queryColdDataList);
        resultMap.put("recordsTotal", countColdDataList); // 返回没有任何过滤条件的总数
        return resultMap;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:保存或更新Streaming任务告警信息
     */
    @Override
    public Map<String, Object> saveOrUpdateStreamAlarm(
            Integer id,
            String taskId,
            String groupId,
            String project,
            String developUser,
            String rangeStr,
            String crUser) {
        if (id == null) {
            clusterYarnMapper.insertStreamAlarm("", groupId, project, developUser, rangeStr, crUser);
        } else {
            clusterYarnMapper.updateStreamAlarm("", groupId, project, developUser, rangeStr, id);
        }
        Map<String, Object> res = new HashMap<>();
        res.put("errorCode", 0);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:删除streaming任务告警数据
     */
    @Override
    public Map<String, Object> deleteStreamAlarm(Integer id) {
        clusterYarnMapper.deleteStreamAlarm(id);
        Map<String, Object> res = new HashMap<>();
        res.put("errorCode", 0);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:获取Streaming任务告警数据
     */
    @Override
    public Map<String, Object> getStreamAlarmDetail(Integer id) {
        Map<String, Object> streamAlarmDetail = clusterYarnMapper.getStreamAlarmDetail(id);
        return streamAlarmDetail;
    }
}
