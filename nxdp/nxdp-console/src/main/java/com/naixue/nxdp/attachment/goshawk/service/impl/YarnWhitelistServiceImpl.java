package com.naixue.nxdp.attachment.goshawk.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.Predicate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.naixue.nxdp.attachment.goshawk.dao.YarnWhiltelistRepository;
import com.naixue.nxdp.attachment.goshawk.model.Whitelist;
import com.naixue.nxdp.attachment.goshawk.service.IYarnWhitelistService;

/**
 * @author 刘蒙
 */
@Service
public class YarnWhitelistServiceImpl implements IYarnWhitelistService {

    @Autowired
    private YarnWhiltelistRepository whiltelistRepository;

    @Override
    public Page<Whitelist> list(Whitelist condition, Integer pageIndex, Integer pageSize) {
        Specification<Whitelist> specification =
                (root, query, cb) -> {
                    if (condition == null) {
                        return null;
                    }
                    List<Predicate> data = new ArrayList<>();

                    // 任务名
                    if (!StringUtils.isEmpty(condition.getAppName())) {
                        if (!StringUtils.isEmpty(condition.getAppName())) {
                            data.add(cb.like(root.get("appName"), "%" + condition.getAppName() + "%"));
                        }
                    }

                    // 负责人
                    if (!StringUtils.isEmpty(condition.getAuthorName())) {
                        if (!condition.getAuthorName().equals("0")) {
                            data.add(cb.equal(root.get("authorName"), condition.getAuthorName()));
                        }
                    }

                    return cb.and(data.toArray(new Predicate[data.size()]));
                };
        return whiltelistRepository.findAll(
                specification,
                new PageRequest(
                        pageIndex, pageSize, new Sort(new Sort.Order(Sort.Direction.DESC, "gmtCreate"))));
    }
}
