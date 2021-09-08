package com.naixue.nxdp.attachment.goshawk.controller;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.naixue.nxdp.annotation.Admin;
import com.naixue.nxdp.model.SearchParam;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.service.OldPageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.naixue.nxdp.attachment.goshawk.dao.mapper.ClusterYarnMapper;
import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsCold;
import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsColdWhite;
import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsMerge;
import com.naixue.nxdp.attachment.goshawk.service.ClusterHdfsColdService;
import com.naixue.nxdp.attachment.goshawk.service.IAsyncService;
import com.naixue.nxdp.attachment.goshawk.service.IGoshawkService;
import com.naixue.nxdp.attachment.util.DateUtil;
import com.naixue.nxdp.web.BaseController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/goshawk")
@PropertySource(value = {"classpath:/profiles/${spring.profiles.active}/goshawk.properties"})
public class GoshawkController extends BaseController {

    @Autowired
    private IGoshawkService goshawkServiceImpl;

    @Autowired
    private OldPageService oldPageService;

    @Autowired
    private IAsyncService asyncServiceImpl;

    @Autowired
    private ClusterHdfsColdService clusterHdfsColdService;

    @Autowired
    private ClusterYarnMapper clusterYarnMapper;

    @Value("${cold.task.id}")
    private String cold_task_id;

    @Value("${little_dir.task.id}")
    private String little_dir_task_id;

    /**
     * @author wangkaixuan
     * @date 2018年12月14日 @Description:苍鹰首页
     */
    @RequestMapping("/index")
    public ModelAndView kafkaMonitor() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/index");

        String yes7day = DateUtil.getNDaysAgo(7, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        String yesDay = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String yesyesDay = DateUtil.getNDaysAgo(2, "yyyy-MM-dd");
        String[] xList = new String[7];
        String[] xListStr = new String[7];
        try {
            Set<String> dashDateRangeSet = DateUtil.getDashDateRangeSet(yes7day, yesDay);
            dashDateRangeSet.toArray(xList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Map<String, Object> appCount =
                goshawkServiceImpl.getAppNumCount(yes7day, today, yesDay, yesyesDay, xList);

        for (int i = 0; i < xList.length; i++) {
            xListStr[i] = "'" + xList[i] + "'";
        }

        mv.addObject("xData", JSONObject.toJSON(xList));
        mv.addObject("ddData", Arrays.toString((Long[]) appCount.get("ddData")));
        mv.addObject("fddData", Arrays.toString((Long[]) appCount.get("fddData")));
        mv.addObject("allData", Arrays.toString((Long[]) appCount.get("allData")));
        // mv.addObject("querySelectList",selectList);
        // System.out.println(goshawkServiceImpl.countGosh());
        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月11日 @Description: 获取首页任务运行统计
     */
    @RequestMapping("/getIndexReportData.do")
    @ResponseBody
    public Object getIndexReportData() {
        Map<String, Object> res = new HashMap<>();
        String[] xList = new String[7];
        String yes7day = DateUtil.getNDaysAgo(7, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        String yesDay = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String yesyesDay = DateUtil.getNDaysAgo(2, "yyyy-MM-dd");
        Set<String> dashDateRangeSet = null;
        try {
            int i = 0;
            dashDateRangeSet = DateUtil.getDashDateRangeSet(yes7day, yesDay);
            for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                xList[i] = string;
                i++;
            }
            // dashDateRangeSet.toArray(xList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        res.put("xList", dashDateRangeSet);

        // 获取App日运行量：块状图、折线图、柱状图数据
        res = goshawkServiceImpl.getAppNumCount(yes7day, today, yesDay, yesyesDay, xList);

        Map<String, Object> longTimeTaskCount =
                goshawkServiceImpl.getLongTimeTaskCount(yes7day, today, yesDay, yesyesDay, xList);

        res.putAll(longTimeTaskCount);

        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月11日 @Description: 获取首页任务运行统计
     */
    @RequestMapping("/getIndexPieData.do")
    @ResponseBody
    public Object getIndexPieData(Integer pieType) {
        Map<String, Object> res = new HashMap<>();
        String[] xList = new String[7];
        String yes7day = DateUtil.getNDaysAgo(7, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        String yesDay = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String yesyesDay = DateUtil.getNDaysAgo(2, "yyyy-MM-dd");
        Set<String> dashDateRangeSet = null;
        try {
            int i = 0;
            dashDateRangeSet = DateUtil.getDashDateRangeSet(yes7day, yesDay);
            for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                xList[i] = string;
                i++;
            }
            // dashDateRangeSet.toArray(xList);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        res.put("xList", dashDateRangeSet);

        // 获取App日运行量：块状图、折线图、柱状图数据
        res = goshawkServiceImpl.getRunTimePai(yes7day, today, yesDay, yesyesDay, pieType);

        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月13日 @Description:获取hdfs使用量统计
     */
    @RequestMapping("/getHdfsPostCountData.do")
    @ResponseBody
    public Object getHdfsPostCountData() {
        Map<String, Object> res = new HashMap<>();
        String[] xList = new String[7];
        String yes7day = DateUtil.getNDaysAgo(7, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        String yesDay = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String yesyesDay = DateUtil.getNDaysAgo(2, "yyyy-MM-dd");
        Set<String> dashDateRangeSet = null;
        try {
            int i = 0;
            dashDateRangeSet = DateUtil.getDashDateRangeSet(yes7day, yesDay);
            for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                xList[i] = string;
                i++;
            }
            // dashDateRangeSet.toArray(xList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获取App日运行量：块状图、折线图、柱状图数据
        res = goshawkServiceImpl.getHdfsReport(yes7day, today, yesDay, yesyesDay, xList);
        res.put("xData", dashDateRangeSet);

        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月18日 @Description:小文件统计
     */
    @RequestMapping("/getHdfsLittleData.do")
    @ResponseBody
    public Object getHdfsLittleData(Integer dataType) {
        Map<String, Object> res = new HashMap<>();
        String[] xList = new String[7];
        String yes7day = DateUtil.getNDaysAgo(7, "yyyy-MM-dd");
        String today = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        String yesDay = DateUtil.getNDaysAgo(1, "yyyy-MM-dd");
        String yesyesDay = DateUtil.getNDaysAgo(2, "yyyy-MM-dd");
        Set<String> dashDateRangeSet = null;
        try {
            int i = 0;
            dashDateRangeSet = DateUtil.getDashDateRangeSet(yes7day, yesDay);
            for (Iterator iterator = dashDateRangeSet.iterator(); iterator.hasNext(); ) {
                String string = (String) iterator.next();
                xList[i] = string;
                i++;
            }
            // dashDateRangeSet.toArray(xList);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // 获取App日运行量：块状图、折线图、柱状图数据
        res =
                goshawkServiceImpl.getHdfsLittleDirReport(
                        yes7day, today, yesDay, yesyesDay, xList, dataType);
        res.put("xData", dashDateRangeSet);

        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月14日 @Description:拉取冷数据
     */
    @ResponseBody
    @RequestMapping("/getColdDataByTask.do")
    public Object getColdDataByTask(HttpServletRequest request, String coldTime) {

        Map<String, Object> res = new HashMap<>();
        res.put("errorCode", 0);
        // 查看目前是否有正在删除中的数据
        Long countColdDataByStatus = goshawkServiceImpl.countColdDataByStatus(1, null);
        if (countColdDataByStatus > 0) {
            res.put("errorCode", -1);
            res.put("message", "拉取失败，目前有还有" + countColdDataByStatus + "条冷数据正在删除，请稍候操作");
            return res;
        }
        Integer jobId = Integer.parseInt(cold_task_id);
        // 查看上次抽取任务是否执行完毕
        Integer coldDataTaskStatus = goshawkServiceImpl.getColdDataTaskStatus(jobId);
        if (coldDataTaskStatus != null
                && (coldDataTaskStatus == 0
                || coldDataTaskStatus == 1
                || coldDataTaskStatus == 2
                || coldDataTaskStatus == 6)) {
            res.put("errorCode", -1);
            res.put("message", "拉取失败，目前存在未执行完毕的拉取任务，请稍候重试");
            return res;
        }

        // 拉取冷数据 cold_task_id
        User user = getCurrentUser(request);
        // 从detail中解析运行时间
        Timestamp chooseRunTime = null;
        // JSONObject detailJson = JSONObject.parseObject(detail);
        if (StringUtils.isNotBlank(coldTime)) {
            try {
                chooseRunTime = new Timestamp(DateUtil.format3.parse(coldTime).getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        String result = oldPageService.runJobManual(jobId, chooseRunTime, user);
        if ("false".equals(result)) {
            res.put("errorCode", -1);
            res.put("message", "拉取失败，任务异常请联系管理员");
            return res;
        }

        return res;
    }

    /**
     * @throws Exception
     * @author wangkaixuan
     * @date 2018年12月14日 @Description:删除冷数据
     */
    @Admin
    @ResponseBody
    @RequestMapping("/delColdData.do")
    public Object delColdData(String ids) throws Exception {
        Map<String, Object> res = new HashMap<>();
        // 更新冷数据状态
        if (StringUtils.isNotBlank(ids)) {
            ids = ids.substring(0, ids.lastIndexOf(","));
        } else {
            res.put("errorCode", -1);
            res.put("message", "删除失败，请选择要删除的冷数据");
            return res;
        }
        // 查看删除中的数据是否存在未结束删除任务
        Long countColdDataByStatus = goshawkServiceImpl.countColdDataByStatus(1, ids);
        if (countColdDataByStatus > 0) {
            res.put("errorCode", -1);
            res.put("message", "删除失败，所选数据中包含删除中数据，请刷新页面重新选择");
            return res;
        }
        Long countColdDataByStatus2 = goshawkServiceImpl.countColdDataByStatus(2, ids);
        if (countColdDataByStatus2 > 0) {
            res.put("errorCode", -1);
            res.put("message", "删除失败，所选数据中包含已删除数据，请刷新页面重新选择");
            return res;
        }
        // 白名单过滤
        List<ClusterHdfsCold> colds = clusterYarnMapper.findClusterHdfsColdsByIds(ids);
        clusterHdfsColdService.filterColdsWithWhites(colds);
        if (!CollectionUtils.isEmpty(colds)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < colds.size(); i++) {
                sb.append(colds.get(i).getId());
                if (i < colds.size() - 1) {
                    sb.append(",");
                }
            }
            ids = sb.toString();
        }
        if (!Strings.isNullOrEmpty(ids)) {
            log.debug("冷数据删除：{}", ids);
            goshawkServiceImpl.updateColdData(ids, 1);
            // 查询数据
            List<Map<String, Object>> delColdDataList = goshawkServiceImpl.getDelColdDataList(ids);
            // 开始删除数据
            asyncServiceImpl.delColdDir(delColdDataList);
        }
        res.put("errorCode", 0);
        return res;
    }

    @Admin
    @ResponseBody
    @RequestMapping("/cluster/hdfs/cold/forceDelete.do")
    public Object forceDeleteColdData(String ids) throws Exception {
        List<Integer> coldIds = new ArrayList<>();
        Iterator<String> it = Splitter.on(",").split(ids).iterator();
        while (it.hasNext()) {
            coldIds.add(Integer.parseInt(it.next()));
        }
        clusterHdfsColdService.forceDeleteColds(coldIds);
        return success();
    }

    @Admin
    @ResponseBody
    @RequestMapping("/cluster/hdfs/cold/deleteAll.do")
    public Object delAllColdData(String dirLike, Integer status) throws Exception {
        clusterHdfsColdService.deleteColds(dirLike, status);
        return success();
    }

    @RequestMapping("/cluster/hdfs/cold/client")
    public Object coldDataClient() {
        return "attachment/goshawk/cold-data-client";
    }

    @ResponseBody
    @RequestMapping("/cluster/hdfs/cold/list.do")
    public Object listColds(DataTableRequest dataTable, ClusterHdfsCold condition) {
        Page<ClusterHdfsCold> page =
                clusterHdfsColdService.listColds(
                        condition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<ClusterHdfsCold>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月17日 @Description:跳转冷数据
     */
    @Admin
    @RequestMapping("/coldDataPage")
    public ModelAndView coldDataPage(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("attachment/goshawk/coldDataPage");
        // 查看目前是否有正在删除中的数据
        Long countColdDataByStatus = goshawkServiceImpl.countColdDataByStatus(1, null);
        if (countColdDataByStatus == 0) {
            // 目前没有删除中的数据
            mv.addObject("waitDelData", 0);
        } else {
            // 目前存在删除中的数据
            mv.addObject("waitDelData", countColdDataByStatus);
        }
        return mv;
    }

    /**
     * 列表页 - 返回任务搜索结果
     *
     * @param searchParam 搜索条件
     * @return
     */
    @RequestMapping("/search-colddata.do")
    @ResponseBody
    public Object searchColdData(SearchParam searchParam, Integer isDel, String coldDir) {
        // 获取指定条件的任务明细 及 记录总数
        Pageable pageable =
                new PageRequest(
                        searchParam.getStart() / searchParam.getLength(),
                        searchParam.getLength(),
                        Sort.Direction.DESC,
                        "createTime");

        // Page<JobSchedule> data = jobScheduleRepository.findAll(getJobScheduleSpec(searchParam),
        // pageable);
        Map<String, Object> resultMap =
                goshawkServiceImpl.getColdDataList(
                        pageable,
                        (isDel == null || isDel == -1) ? null : isDel,
                        StringUtils.isBlank(coldDir) ? null : coldDir);
        // resultMap.put("recordsFiltered", data.getTotalElements()); // 返回搜索条件查询结果数
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        // resultMap.put("data", data.getContent());
        // resultMap.put("recordsTotal", data.getTotalElements()); // 返回没有任何过滤条件的总数
        return resultMap;
    }

    @ResponseBody
    @RequestMapping("/cluster/hdfs/coldWhite/list.do")
    public Object listColdWhites(DataTableRequest dataTable, ClusterHdfsColdWhite condition) {
        Page<ClusterHdfsColdWhite> page =
                clusterHdfsColdService.listColdWhites(
                        condition, dataTable.getStart() / dataTable.getLength(), dataTable.getLength());
        return new DataTableResponse<ClusterHdfsColdWhite>(
                dataTable.getStart(),
                dataTable.getLength(),
                dataTable.getDraw(),
                page.getTotalElements(),
                page.getTotalElements(),
                page.getContent());
    }

    @ResponseBody
    @RequestMapping("/cluster/hdfs/coldWhite/register.do")
    public Object registerColdWhite(HttpServletRequest request, String keywords) {
        User currentUser = getCurrentUser(request);
        clusterHdfsColdService.registerColdWhite(currentUser, keywords);
        return success();
    }

    @ResponseBody
    @RequestMapping("/cluster/hdfs/coldWhite/delete.do")
    public Object deleteColdWhite(HttpServletRequest request, Integer coldWhiteId) {
        User currentUser = getCurrentUser(request);
        clusterHdfsColdService.deleteColdWhite(currentUser, coldWhiteId);
        return success();
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月19日 @Description:跳转hdfs管理报表页面
     */
    @RequestMapping("/hdfsReport")
    public ModelAndView hdfsReport() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/hdfsReport");

        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月19日 @Description:跳转hdfs小文件管理报表页面
     */
    @RequestMapping("/hdfsLittleReport")
    public ModelAndView hdfsLittleReport() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/hdfsLittleReport");

        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月19日 @Description:搜索各组目录文件数 menuType 1：文件数 2：小文件数
     */
    @RequestMapping("/searchHdfsFileNum.do")
    @ResponseBody
    public Object searchHdfsFileNum(Integer menuType) {
        // 获取指定条件的任务明细 及 记录总数
        Map<String, Object> res = new HashMap<>();
        res = goshawkServiceImpl.getHdfsFileNumData(menuType);
        // Map<String, Object> resultMap =
        // goshawkServiceImpl.getColdDataList(pageable,isDel,StringUtils.isBlank(coldDir)?null:coldDir);
        // resultMap.put("start", searchParam.getStart());
        // resultMap.put("pageSize", searchParam.getLength());
        // resultMap.put("draw", searchParam.getDraw()); //
        // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月19日 @Description:查询某组目录文件详情
     */
    @RequestMapping("/searchHdfsFileNumDetail.do")
    @ResponseBody
    public Object searchHdfsFileNumDetail(String dirStr, String maxDate, Integer menuType) {

        Map<String, Object> res =
                goshawkServiceImpl.getHdfsFileDetailNumData(dirStr, maxDate, menuType);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:跳转小文件列表页面
     */
    @RequestMapping("/littleDirPage")
    public ModelAndView littleDirPage() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/littleDirPage");
        // 查看目前是否有正在删除中的数据
        Long countLittleDataByStatus = goshawkServiceImpl.countLittleDirList("'1'", null, null);
        if (countLittleDataByStatus == 0) {
            // 目前没有删除中的数据
            mv.addObject("waitMergeData", 0);
        } else {
            // 目前存在删除中的数据
            mv.addObject("waitMergeData", countLittleDataByStatus);
        }
        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:查找小文件目录列表
     */
    @RequestMapping("/searchLittleDirList.do")
    @ResponseBody
    public Object searchLittleDirList(String dirStr, Integer isMerge) {
        Map<String, Object> res = new HashMap<>();
        List<Map<String, Object>> littleDirList =
                goshawkServiceImpl.getLittleDirList(isMerge, dirStr, null);
        res.put("data", littleDirList);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:拉取小文件列表
     */
    @ResponseBody
    @RequestMapping("/getLittleDataByTask.do")
    public Object getLittleDataByTask(HttpServletRequest request, Integer littleNum) {

        Map<String, Object> res = new HashMap<>();
        res.put("errorCode", 0);
        // 查看目前是否有正在删除中的数据
        Long countLittleDataByStatus = goshawkServiceImpl.countLittleDirList("'1'", null, null);
        if (countLittleDataByStatus > 0) {
            res.put("errorCode", -1);
            res.put("message", "拉取失败，目前有还有" + countLittleDataByStatus + "条小文件目录合并中，请稍候操作");
            return res;
        }
        Integer jobId = Integer.parseInt(little_dir_task_id);
        // 查看上次抽取任务是否执行完毕
        Integer coldDataTaskStatus = goshawkServiceImpl.getColdDataTaskStatus(jobId);
        if (coldDataTaskStatus != null
                && (coldDataTaskStatus == 0
                || coldDataTaskStatus == 1
                || coldDataTaskStatus == 2
                || coldDataTaskStatus == 6)) {
            res.put("errorCode", -1);
            res.put("message", "拉取失败，目前存在未执行完毕的拉取任务，请稍候重试");
            return res;
        }

        // 拉取冷数据 cold_task_id
        User user = getCurrentUser(request);
        // 从detail中解析运行时间
        Timestamp chooseRunTime = null;
        // JSONObject detailJson = JSONObject.parseObject(detail);
    /*if (StringUtils.isNotBlank(coldTime)) {
          try {
          	chooseRunTime = new Timestamp(DateUtil.format3.parse(coldTime).getTime());
    } catch (ParseException e) {
    	e.printStackTrace();
    }
       }*/
        String result = oldPageService.runJobManual(jobId, chooseRunTime, user);
        if ("false".equals(result)) {
            res.put("errorCode", -1);
            res.put("message", "拉取失败，任务异常请联系管理员");
            return res;
        }

        return res;
    }

    /**
     * @throws Exception
     * @author wangkaixuan
     * @date 2018年12月21日 @Description:合并小文件
     */
    @ResponseBody
    @RequestMapping("/mergeLittleDir.do")
    public Object mergeLittleDir(String ids, String dir) throws Exception {
        Map<String, Object> res = new HashMap<>();

        if (StringUtils.isNotBlank(dir)) {
            // 合并选中目录
            ClusterHdfsMerge chm = new ClusterHdfsMerge();
            chm.setDir(dir);
            chm.setFileNum(0l);
            chm.setNum(0l);
            chm.setIsMerge(1);
            chm = goshawkServiceImpl.saveClusterHdfsMerge(chm);
            List<Map<String, Object>> dataList = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("id", chm.getId());
            map.put("dir", chm.getDir());
            map.put("num", chm.getNum());
            map.put("file_num", chm.getFileNum());
            map.put("is_merge", chm.getIsMerge());
            dataList.add(map);
            // 查询数据
            asyncServiceImpl.mergeLittleDir(dataList);
            res.put("errorCode", 0);
        } else {
            // 更新小文件状态
            if (StringUtils.isNotBlank(ids)) {
                ids = ids.substring(0, ids.lastIndexOf(","));
            } else {
                res.put("errorCode", -1);
                res.put("message", "合并失败，请选择要合并的小文件");
                return res;
            }
            // 查看小文件状态个数
            Long countColdDataByStatus = goshawkServiceImpl.countLittleDirList("'1','2'", null, ids);
            if (countColdDataByStatus > 0) {
                res.put("errorCode", -1);
                res.put("message", "合并失败，所选数据中包含合并中和合并成功数据，请刷新页面重新选择");
                return res;
            }

            goshawkServiceImpl.updateLittleDir(ids, 1);

            // 查询数据
            List<Map<String, Object>> mergeDirList = goshawkServiceImpl.getLittleDirList(null, null, ids);

            // 开始合并数据
            asyncServiceImpl.mergeLittleDir(mergeDirList);
            res.put("errorCode", 0);
        }
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月3日 @Description:获取各队列任务百分比数据
     */
    @RequestMapping("/getTaskQueueData.do")
    @ResponseBody
    public Object getTaskQueueData() {
        Map<String, Object> res = new HashMap<>();
        // 获取各队列任务百分比
        res = goshawkServiceImpl.getTaskPieReportData();
        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月3日 @Description:获取昨日24小时每个小时的任务数
     */
    @RequestMapping("/get24HourTaskData.do")
    @ResponseBody
    public Object get24HourTaskData(Integer reprotType, String reportDate) {
        Map<String, Object> res = new HashMap<>();
        // 获取昨日24小时每个小时的任务数
        if (StringUtils.isBlank(reportDate)) {
            reportDate = DateUtil.getNDaysAgo(0, "yyyy-MM-dd");
        }

        res = goshawkServiceImpl.get24HoursTaskReportData(reprotType, reportDate);

        if (StringUtils.isNotBlank(reportDate)) {
            res.put("reportDate", reportDate);
        }
        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月3日 @Description:获取各部门Job数
     */
    @RequestMapping("/getDeptTaskData.do")
    @ResponseBody
    public Object getDeptTaskData(Integer reprotType) {
        Map<String, Object> res = new HashMap<>();
        // 获取各部门Job数
        res = goshawkServiceImpl.getDeptJobReprotData(reprotType);
        return success("data", res);
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月4日 @Description:24小时任务数统计
     */
    @RequestMapping("/hour24TaskReportPage")
    public ModelAndView hour24TaskReportPage() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/hour24TaskReport");
        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:
     */
    @RequestMapping("/flumeTaskManage")
    public ModelAndView flumeTaskManage() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/flumeTaskManage");

        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:查询flume管理数据列表
     */
    @RequestMapping("/searchFlumeTaskManage.do")
    @ResponseBody
    public Object searchFlumeTaskManage(SearchParam searchParam, String urlStr, String rangeStr) {
        // 获取指定条件的任务明细 及 记录总数
        Pageable pageable =
                new PageRequest(
                        searchParam.getStart() / searchParam.getLength(),
                        searchParam.getLength(),
                        Sort.Direction.DESC,
                        "createTime");

        Map<String, Object> resultMap =
                goshawkServiceImpl.getFlumeManageList(
                        pageable,
                        StringUtils.isBlank(urlStr) ? null : ("%" + urlStr + "%"),
                        StringUtils.isBlank(rangeStr) ? null : ("%" + rangeStr + "%"));
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        return resultMap;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:
     */
    @RequestMapping("/saveOrUpdateFlumeManage.do")
    @ResponseBody
    public Object saveOrUpdateFlumeManage(
            String urlStr, String rangeStr, Integer fid, HttpServletRequest request) {
        User user = getCurrentUser(request);
        goshawkServiceImpl.saveOrUpdateFlumeManage(fid, urlStr, rangeStr, user.getPyName());
        return success("ok", "msg", "");
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:删除flume管理数据
     */
    @ResponseBody
    @RequestMapping("/delFlumeManage.do")
    public Object delFlumeManage(Integer fid) {
        Map<String, Object> res = new HashMap<>();

        if (fid == null) {
            res.put("errorCode", -1);
            res.put("message", "删除失败，数据不存在，请刷新页面重新操作");
            return res;
        }

        res = goshawkServiceImpl.deleteFlumeManage(fid);

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:获取flume管理数据详情
     */
    @ResponseBody
    @RequestMapping("/getFlumeManageDetail.do")
    public Object getFlumeManageDetail(Integer fid) {
        Map<String, Object> res = new HashMap<>();
        res = goshawkServiceImpl.getFlumeManageDetail(fid);
        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:跳转Streaming任务告警页面
     */
    @RequestMapping("/streamAlarmManage")
    public ModelAndView streamAlarmManage() {
        ModelAndView mv = new ModelAndView("attachment/goshawk/streamAlarmManage");

        return mv;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:查询Streaming任务告警数据列表
     */
    @RequestMapping("/searchStreamAlarm.do")
    @ResponseBody
    public Object searchStreamAlarm(
            SearchParam searchParam,
            String taskId,
            String groupId,
            String project,
            String developUser,
            String rangeStr) {
        // 获取指定条件的任务明细 及 记录总数
        Pageable pageable =
                new PageRequest(
                        searchParam.getStart() / searchParam.getLength(),
                        searchParam.getLength(),
                        Sort.Direction.DESC,
                        "createTime");

        Map<String, Object> resultMap =
                goshawkServiceImpl.getStreamAlarmList(
                        pageable,
                        StringUtils.isBlank(taskId) ? null : "%" + taskId + "%",
                        StringUtils.isBlank(groupId) ? null : "%" + groupId + "%",
                        StringUtils.isBlank(project) ? null : "%" + project + "%",
                        StringUtils.isBlank(developUser) ? null : "%" + developUser + "%",
                        StringUtils.isBlank(rangeStr) ? null : "%" + rangeStr + "%");
        resultMap.put("start", searchParam.getStart());
        resultMap.put("pageSize", searchParam.getLength());
        resultMap.put(
                "draw",
                searchParam
                        .getDraw()); // 返回前端ajax请求顺序标识数（可能同时存在多个ajax请求，而无法判断dataTable和后端数据的一一映射关系，所以用draw判断）
        return resultMap;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月5日 @Description:保存或更新Streaming任务告警数据
     */
    @RequestMapping("/saveOrUpdateStreamAlarm.do")
    @ResponseBody
    public Object saveOrUpdateStreamAlarm(
            String taskId,
            String groupId,
            String project,
            String developUser,
            String rangeStr,
            Integer sid,
            HttpServletRequest request) {
        User user = getCurrentUser(request);
        goshawkServiceImpl.saveOrUpdateStreamAlarm(
                sid, taskId, groupId, project, developUser, rangeStr, user.getPyName());
        return success("ok", "msg", "");
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:删除Streaming任务告警数据
     */
    @ResponseBody
    @RequestMapping("/delStreamAlarm.do")
    public Object delStreamAlarm(Integer sid) {
        Map<String, Object> res = new HashMap<>();

        if (sid == null) {
            res.put("errorCode", -1);
            res.put("message", "删除失败，数据不存在，请刷新页面重新操作");
            return res;
        }

        res = goshawkServiceImpl.deleteStreamAlarm(sid);

        return res;
    }

    /**
     * @author wangkaixuan
     * @date 2019年1月7日 @Description:获取flume管理数据详情
     */
    @ResponseBody
    @RequestMapping("/getStreamAlarmDetail.do")
    public Object getStreamAlarmDetail(Integer sid) {
        Map<String, Object> res = new HashMap<>();
        res = goshawkServiceImpl.getStreamAlarmDetail(sid);
        return res;
    }
}
