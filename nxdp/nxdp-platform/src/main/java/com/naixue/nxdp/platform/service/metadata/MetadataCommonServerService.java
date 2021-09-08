package com.naixue.nxdp.platform.service.metadata;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.common.base.Strings;
import com.naixue.zzdp.common.util.SecurityUtils;
import com.naixue.nxdp.data.Status;
import com.naixue.nxdp.data.dao.metadata.MetadataCommonServerDao;
import com.naixue.nxdp.data.model.metadata.MetadataCommonServer;
import com.naixue.nxdp.data.model.metadata.MetadataType;
import com.naixue.nxdp.data.util.JdbcTemplateUtils;

@Service
public class MetadataCommonServerService {

  @Autowired private MetadataCommonServerDao metadataCommonServerDao;

  public Page<MetadataCommonServer> listMetadataCommonServers(
      MetadataCommonServer condition, Integer pageIndex, Integer pageSize, Sort.Order... orders) {
    Specification<MetadataCommonServer> specification =
        new Specification<MetadataCommonServer>() {
          @Override
          public Predicate toPredicate(
              Root<MetadataCommonServer> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            if (Objects.isNull(condition)) {
              return null;
            }
            List<Predicate> data = new ArrayList<>();
            if (!Strings.isNullOrEmpty(condition.getSearch())) {
              data.add(cb.like(root.get("name"), "%" + condition.getSearch() + "%"));
            }
            return cb.and(data.toArray(new Predicate[data.size()]));
          }
        };
    Pageable pageable = new PageRequest(pageIndex, pageSize, new Sort(orders));
    return metadataCommonServerDao.findAll(specification, pageable);
  }

  public List<MetadataCommonServer> listMetadataCommonServers(final MetadataType metadataType) {
    return metadataCommonServerDao.findByType(metadataType.getCode());
  }

  public MetadataCommonServer getMetadataCommonServerByServerId(final Integer id) {
    return metadataCommonServerDao.findOne(id);
  }

  @org.springframework.transaction.annotation.Transactional
  public MetadataCommonServer saveMetadataCommonServer(final MetadataCommonServer server) {
    if (server.getId() == null) { // 重名校验
      MetadataCommonServer exist =
          metadataCommonServerDao.findTop1ByStatusAndName(
              Integer.parseInt(Status.NORMAL.toString()), server.getName());
      if (exist != null) {
        throw new RuntimeException("重名");
      }
    }
    JdbcTemplate jdbcTemplate =
        JdbcTemplateUtils.createJdbcTemplate(
            JdbcTemplateUtils.DbType.valueOf(server.getMetadataType().getName()),
            server.getHost(),
            server.getPort(),
            null,
            server.getUsername(),
            server.getPassword());
    jdbcTemplate.execute("SELECT 1");
    if (server.getId() == null) { // 新建
      server.setStatus(Integer.valueOf(Status.NORMAL.toString())); // 默认正常状态
    } else { // 原数据中不可变更的信息同步到新数据中
      MetadataCommonServer source = metadataCommonServerDao.findOne(server.getId());
      server.setCreator(source.getCreator());
    }
    // 对密码进行加密处理
    server.setPassword(SecurityUtils.simpleEncrypt(server.getPassword()));
    return metadataCommonServerDao.save(server);
  }
}
