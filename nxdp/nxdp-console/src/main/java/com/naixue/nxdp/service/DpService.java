package com.naixue.nxdp.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.naixue.zzdp.common.util.SecurityUtils;
import com.naixue.nxdp.dao.ServerConfigRepository;
import com.naixue.zzdp.data.util.JdbcTemplateUtils;
import com.naixue.nxdp.model.ServerConfig;
import com.naixue.nxdp.model.User;

@Deprecated
@Service
public class DpService {

    @Autowired
    private ServerConfigRepository serverConfigRepository;

    @Transactional
    public void saveServerConfig(User currentUser, ServerConfig s) throws Exception {
        Assert.notNull(currentUser, "当前登录用户无数据");
        Assert.notNull(s, "添加的服务器配置信息为空");
        // 校验服务器别名是否存在
        ServerConfig exist = serverConfigRepository.findByName(s.getName());
        if (exist != null && !exist.getId().equals(s.getId())) {
            throw new RuntimeException("服务器别名已经存在。");
        }
        // 校验服务器配置是否正确
        if (ServerConfig.ServerType.MYSQL.getId().equals(s.getServerType())) {
            JdbcTemplate jdbcTemplate =
                    JdbcTemplateUtils.createJdbcTemplate(
                            s.getHost(),
                            s.getPort(),
                            null,
                            ServerConfig.ServerType.MYSQL.getName(),
                            "com.mysql.jdbc.Driver",
                            s.getUsername(),
                            s.getPassword());
            jdbcTemplate.execute("SELECT 1 FROM dual");
        }
        s.setPassword(SecurityUtils.simpleEncrypt(s.getPassword()));
        s.setCreateTime(new Date());
        s.setModifyTime(new Date());
        s.setCreatorId(currentUser.getId());
        s.setLastModifierId(currentUser.getId());
        serverConfigRepository.save(s);
    }
}
