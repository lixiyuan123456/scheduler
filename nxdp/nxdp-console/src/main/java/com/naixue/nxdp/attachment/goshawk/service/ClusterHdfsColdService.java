package com.naixue.nxdp.attachment.goshawk.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaBuilder.In;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsCold;
import com.naixue.nxdp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.naixue.nxdp.attachment.goshawk.dao.ClusterHdfsColdRepository;
import com.naixue.nxdp.attachment.goshawk.dao.ClusterHdfsColdWhiteRepository;
import com.naixue.nxdp.attachment.goshawk.model.ClusterHdfsColdWhite;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ClusterHdfsColdService {

    @Autowired
    private ClusterHdfsColdRepository clusterHdfsColdRepository;

    @Autowired
    private ClusterHdfsColdWhiteRepository clusterHdfsColdWhiteRepository;

    @Autowired
    private IAsyncService asyncService;

    @Autowired
    private IGoshawkService goshawkService;

    public Page<ClusterHdfsCold> listColds(
            ClusterHdfsCold condition, Integer pageIndex, Integer pageSize) {
        Specification<ClusterHdfsCold> specification = buildSpecification(condition);
        return clusterHdfsColdRepository.findAll(specification, new PageRequest(pageIndex, pageSize));
    }

    public List<ClusterHdfsCold> listColds(ClusterHdfsCold condition) {
        Specification<ClusterHdfsCold> specification = buildSpecification(condition);
        return clusterHdfsColdRepository.findAll(specification);
    }

    private Specification<ClusterHdfsCold> buildSpecification(ClusterHdfsCold condition) {
        return new Specification<ClusterHdfsCold>() {
            @Override
            public Predicate toPredicate(
                    Root<ClusterHdfsCold> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> conditions = new ArrayList<>();
                if (!CollectionUtils.isEmpty(condition.getIncludeIds())) {
                    In<Object> in = cb.in(root.get("id"));
                    for (Integer id : condition.getIncludeIds()) {
                        in.value(id);
                    }
                    conditions.add(in);
                }
                if (!StringUtils.isEmpty(condition.getDirLike())) {
                    conditions.add(cb.like(root.get("dir"), "%" + condition.getDirLike() + "%"));
                }
                if (condition.getStatus() != null && condition.getStatus() != -1) {
                    conditions.add(cb.equal(root.get("status"), condition.getStatus()));
                }
                if (!CollectionUtils.isEmpty(condition.getIncludeStatus())) {
                    In<Object> in = cb.in(root.get("status"));
                    for (Integer status : condition.getIncludeStatus()) {
                        in.value(status);
                    }
                    conditions.add(in);
                }
                if (!CollectionUtils.isEmpty(condition.getExcludeStatus())) {
                    In<Object> in = cb.in(root.get("status"));
                    for (Integer status : condition.getExcludeStatus()) {
                        in.value(status);
                    }
                    conditions.add(cb.not(in));
                }
                return cb.and(conditions.toArray(new Predicate[conditions.size()]));
            }
        };
    }

    @Transactional
    public void forceDeleteColds(List<Integer> coldIds) throws Exception {
        if (CollectionUtils.isEmpty(coldIds)) {
            return;
        }
        ClusterHdfsCold condition = new ClusterHdfsCold();
        condition.setIncludeIds(coldIds);
        // 正在删除中、已删除的冷数据需要排除掉
        List<Integer> excludeStatus =
                Arrays.asList(
                        ClusterHdfsCold.Status.DELETING.getCode(),
                        ClusterHdfsCold.Status.DELETE_SUCCESS.getCode());
        condition.setExcludeStatus(excludeStatus);
        List<ClusterHdfsCold> colds = listColds(condition);
        if (CollectionUtils.isEmpty(colds)) {
            return;
        }
        log.debug("删除冷数据过滤白名单前：ID={}，COUNT={}", Joiner.on(",").join(coldIds), coldIds.size());
        filterColdsWithWhites(colds);
        log.debug("删除冷数据过滤白名单后：COUNT={}", colds.size());
        List<Integer> newColdIds = new ArrayList<>();
        for (ClusterHdfsCold c : colds) {
            newColdIds.add(c.getId());
        }
        String strColdIds = Joiner.on(",").join(newColdIds);
        log.debug("删除冷数据过滤白名单后：ID={}", strColdIds);
        goshawkService.updateColdData(strColdIds, ClusterHdfsCold.Status.DELETING.getCode());
        List<Map<String, Object>> dataList = goshawkService.getDelColdDataList(strColdIds);
        asyncService.delColdDir(dataList, true);
    }

    @Transactional
    public void deleteColds(String dirLike, Integer status) throws Exception {
        if (status != null) {
            ClusterHdfsCold.Status st = ClusterHdfsCold.Status.get(status);
            if (st == ClusterHdfsCold.Status.DELETING || st == ClusterHdfsCold.Status.DELETE_SUCCESS) {
                throw new RuntimeException("不允许删除正在删除中、已删除的冷数据");
            }
        }
        ClusterHdfsCold cold = new ClusterHdfsCold();
        cold.setDirLike(dirLike);
        cold.setStatus(status);
        // 正在删除中、已删除的冷数据需要排除掉
        List<Integer> excludeStatus =
                Arrays.asList(
                        ClusterHdfsCold.Status.DELETING.getCode(),
                        ClusterHdfsCold.Status.DELETE_SUCCESS.getCode());
        cold.setExcludeStatus(excludeStatus);
        List<ClusterHdfsCold> colds = listColds(cold);
        if (CollectionUtils.isEmpty(colds)) {
            return;
        }
        log.debug("删除冷数据过滤白名单前：DIR={},STATUS={},COUNT={}", dirLike, status, colds.size());
        filterColdsWithWhites(colds);
        log.debug("删除冷数据过滤白名单后：DIR={},STATUS={},COUNT={}", dirLike, status, colds.size());
        List<Integer> coldIds = new ArrayList<>();
        for (ClusterHdfsCold c : colds) {
            coldIds.add(c.getId());
        }
        String strColdIds = Joiner.on(",").join(coldIds);
        log.debug("删除冷数据过滤白名单后：ID={}", strColdIds);
        goshawkService.updateColdData(strColdIds, ClusterHdfsCold.Status.DELETING.getCode());
        List<Map<String, Object>> dataList = goshawkService.getDelColdDataList(strColdIds);
        asyncService.delColdDir(dataList);
    }

    public void filterColdsWithWhites(List<ClusterHdfsCold> colds) {
        if (CollectionUtils.isEmpty(colds)) {
            return;
        }
        List<ClusterHdfsColdWhite> whites = clusterHdfsColdWhiteRepository.findAll();
        if (CollectionUtils.isEmpty(whites)) {
            return;
        }
        Iterator<ClusterHdfsCold> it = colds.iterator();
        while (it.hasNext()) {
            ClusterHdfsCold cold = it.next();
            for (ClusterHdfsColdWhite white : whites) {
                if (cold.getDir().contains(white.getKeywords())) {
                    it.remove();
                    break;
                }
            }
        }
    }

    public Page<ClusterHdfsColdWhite> listColdWhites(
            ClusterHdfsColdWhite condition, Integer pageIndex, Integer pageSize) {
        return clusterHdfsColdWhiteRepository.findAll(
                new Specification<ClusterHdfsColdWhite>() {
                    @Override
                    public Predicate toPredicate(
                            Root<ClusterHdfsColdWhite> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                        List<Predicate> restrictions = new ArrayList<>();
                        if (!StringUtils.isEmpty(condition.getKeywords())) {
                            restrictions.add(cb.like(root.get("keywords"), "%" + condition.getKeywords() + "%"));
                        }
                        return cb.and(restrictions.toArray(new Predicate[restrictions.size()]));
                    }
                },
                new PageRequest(pageIndex, pageSize, new Sort(new Order(Direction.DESC, "id"))));
    }

    @Transactional
    public void registerColdWhite(User currentUser, String keywords) {
        Assert.hasText(keywords, "白名单关键词不允许为空");
        ClusterHdfsColdWhite white = new ClusterHdfsColdWhite();
        white.setKeywords(keywords);
        white.setCreator(currentUser.getPyName());
        clusterHdfsColdWhiteRepository.save(white);
    }

    @Transactional
    public void deleteColdWhite(User currentUser, Integer coldWhiteId) {
        ClusterHdfsColdWhite coldWhite = clusterHdfsColdWhiteRepository.findOne(coldWhiteId);
        User.PermissionLevel permissionLevel =
                User.PermissionLevel.getPermissionLevel(currentUser.getPermissionLevel());
        if (User.PermissionLevel.ADMIN != permissionLevel
                && !coldWhite.getCreator().equals(currentUser.getPyName())) {
            throw new RuntimeException("非法操作！除管理员外，不可删除他人的白名单设置。");
        }
        clusterHdfsColdWhiteRepository.delete(coldWhiteId);
    }

    @Transactional
    public void deleteClusterHdfsColdWhites(User currentUser, Integer[] coldWhiteIds) {
        if (coldWhiteIds != null && coldWhiteIds.length > 0) {
            for (Integer coldWhiteId : coldWhiteIds) {
                deleteColdWhite(currentUser, coldWhiteId);
            }
        }
    }
}
