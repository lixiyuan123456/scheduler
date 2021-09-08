package com.naixue.nxdp.platform.service.zstream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Strings;
import com.naixue.nxdp.data.dao.zstream.ZstreamUdxDao;
import com.naixue.nxdp.data.model.zstream.ZstreamUdx;

@Service
public class ZstreamUdxService {

  @Autowired private ZstreamUdxDao zstreamUdxDao;

  public Page<ZstreamUdx> list(ZstreamUdx condition, Integer pageIndex, Integer pageSize) {
    return list(condition, pageIndex, pageSize, new Sort.Order(Sort.Direction.DESC, "id"));
  }

  public Page<ZstreamUdx> list(
      ZstreamUdx condition, Integer pageIndex, Integer pageSize, Sort.Order... orders) {
    Specification<ZstreamUdx> specification =
        new Specification<ZstreamUdx>() {
          @Override
          public Predicate toPredicate(
              Root<ZstreamUdx> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            if (Objects.isNull(condition)) {
              return null;
            }
            List<Predicate> data = new ArrayList<>();
            data.add(cb.equal(root.get("status"), ZstreamUdx.Status.ALIVE.getCode()));
            if (!Strings.isNullOrEmpty(condition.getProxyCode())
                && !condition.getProxyCode().equals("0")) {
              data.add(cb.equal(root.get("proxyCode"), condition.getProxyCode()));
            }
            if (!Strings.isNullOrEmpty(condition.getCreator())
                && !condition.getCreator().equals("0")) {
              data.add(cb.equal(root.get("creator"), condition.getCreator()));
            }
            if (Objects.nonNull(condition.getType()) && condition.getType() != 0) {
              data.add(cb.equal(root.get("type"), condition.getType()));
            }
            if (!Strings.isNullOrEmpty(condition.getNameLike())) {
              data.add(cb.like(root.get("name"), "%" + condition.getNameLike() + "%"));
            }
            return cb.and(data.toArray(new Predicate[data.size()]));
          }
        };
    Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(orders));
    return zstreamUdxDao.findAll(specification, pageable);
  }

  public ZstreamUdx createUdx(ZstreamUdx udx) {
    return zstreamUdxDao.save(udx);
  }

  public ZstreamUdx findUdxById(Integer udxId) {
    return zstreamUdxDao.findOne(udxId);
  }

  @Transactional
  public void deleteUdx(ZstreamUdx udx) {
    udx.setStatus(ZstreamUdx.Status.DEAD.getCode());
    zstreamUdxDao.save(udx);
  }

  @Transactional
  public void coverPreviousUdx4SameName(String proxyCode, String newUdxName) {
    zstreamUdxDao.deleteByName(proxyCode, newUdxName);
  }
}
