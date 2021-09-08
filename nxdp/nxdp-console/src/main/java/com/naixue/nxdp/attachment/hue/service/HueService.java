package com.naixue.nxdp.attachment.hue.service;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.naixue.nxdp.dao.UserRepository;
import com.naixue.nxdp.model.HadoopBinding;
import com.naixue.nxdp.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import com.naixue.nxdp.attachment.hue.IQueryConfiguration;
import com.naixue.zzdp.common.util.SecurityUtils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 所有通过zzdp跳转到HUE的用户都是通过defaultPassword登录；所有非zzdp跳转到HUE的用户都是自定义的密码登录；
 *
 * @author zhaichuancheng
 */
@Slf4j
@Service
public class HueService {

    // @Autowired HueDb hueDb;

    @Autowired
    private IQueryConfiguration iQueryConfiguration;

    @Autowired
    private UserRepository userRepository;

    // @Autowired private HadoopBindingService hadoopBindingService;

    @Autowired
    private JdbcTemplate hueJdbcTemplate;

    /**
     * 创建HUE自定义账户
     *
     * @param user
     * @param pwd
     */
    @Transactional
    public void createOrModifyHueAccount(User user, String pwd) {
        Assert.notNull(user, "请求参数user不允许为空");
        Assert.hasText(pwd, "请求参数pwd不允许为空");
        // HUE密码md5加密
        String password = DigestUtils.md5DigestAsHex(pwd.getBytes(Charset.defaultCharset()));
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StringBuilder sql = new StringBuilder();
        sql.append(
                "INSERT INTO auth_user (`password`,`last_login`,`is_superuser`,`username`,`first_name`,`last_name`,`email`,`is_staff`,`is_active`,`date_joined`) VALUES(");
        sql.append("'" + password + "'").append(",");
        sql.append("STR_TO_DATE('" + now + "','%Y-%m-%d %H:%i:%s')").append(",");
        sql.append(0).append(",");
        sql.append("'" + user.getPyName() + "'").append(",");
        sql.append("''").append(",");
        sql.append("''").append(",");
        sql.append(user.getEmail() == null ? "''" : "'" + user.getEmail() + "'").append(",");
        sql.append(0).append(",");
        sql.append(1).append(",");
        sql.append("STR_TO_DATE('" + now + "','%Y-%m-%d %H:%i:%s')");
        sql.append(")");
        sql.append(" ON DUPLICATE KEY UPDATE ");
        sql.append(" `username` = '" + user.getPyName() + "'");
        String s = sql.toString();
        log.debug("JDBC:" + s);
        hueJdbcTemplate.update(s);
        // JdbcUtils.executeUpdate(newDbConnection(), s);
    }

    /**
     * 创建HUE默认账户
     *
     * @param user
     */
    @Transactional
    public void createOrModifyDefaultHueAccount(User user) {
        Assert.notNull(user, "请求参数user不允许为空");
        createOrModifyHueAccount(user, newDefaultAccountPassword(user.getPyName()));
        setupHadoopUserGroup(user);
    }

    @Transactional
    public void setupHadoopUserGroup(User user) {
        Assert.notNull(user, "请求参数user不允许为空");
        HadoopBinding hadoopBinding = user.getHadoopBinding();
        // 判断iQuery库用户组是否存在
        Integer authGroupId = findAuthGroupIdByAuthGroupName(hadoopBinding.getProxyCode());
        if (authGroupId == null) {
            createAuthGroupWithDefaultPermissions(hadoopBinding.getProxyCode());
        }
        Integer authUserId = findAuthUserIdByUsername(user.getPyName());
        if (authUserId == null) {
            throw new RuntimeException("用户名=" + user.getPyName() + "在iQurey库auth_user表中不存在");
        }
        authGroupId = findAuthGroupIdByAuthGroupName(hadoopBinding.getProxyCode());
        if (authGroupId == null) {
            throw new RuntimeException(
                    "hadoop用户组名=" + hadoopBinding.getProxyCode() + "在iQurey库auth_group表中不存在");
        }
        String sql =
                "INSERT INTO `auth_user_groups` (`user_id`, `group_id`)VALUES('"
                        + authUserId
                        + "','"
                        + authGroupId
                        + "') ON DUPLICATE KEY UPDATE `user_id` = "
                        + authUserId
                        + ",`group_id` = "
                        + authGroupId;
        hueJdbcTemplate.update(sql);
    /*JdbcUtils.executeUpdate(
    newDbConnection(),
    "INSERT INTO `auth_user_groups` (`user_id`, `group_id`)VALUES('"
        + authUserId
        + "','"
        + authGroupId
        + "') ON DUPLICATE KEY UPDATE `user_id` = "
        + authUserId
        + ",`group_id` = "
        + authGroupId);*/
    }

    /**
     * 同步账户到HUE
     */
    @Transactional
    public void syncZzdpUsers2Hue() {
        List<User> zzdpUsers = userRepository.findAll();
        if (CollectionUtils.isEmpty(zzdpUsers)) {
            return;
        }
        List<Map<String, Object>> hueAccountMaps =
                // JdbcUtils.queryForList(newDbConnection(), "SELECT `username` FROM `auth_user`");
                hueJdbcTemplate.queryForList("SELECT `username` FROM `auth_user`");
        List<String> hueAccounts = new ArrayList<>();
        if (!CollectionUtils.isEmpty(hueAccountMaps)) {
            for (Map<String, Object> hueAccountMap : hueAccountMaps) {
                hueAccounts.add((String) hueAccountMap.get("username"));
            }
        }
        for (User user : zzdpUsers) {
            if (!hueAccounts.contains(user.getPyName())) {
                createOrModifyDefaultHueAccount(user);
            }
        }
    }

    public List<AuthGroup> getAllAuthGroups() {
        List<Map<String, Object>> list =
                // JdbcUtils.queryForList(newDbConnection(), "SELECT `id`,`name` FROM `auth_group`");
                hueJdbcTemplate.queryForList("SELECT `id`,`name` FROM `auth_group`");
        List<AuthGroup> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }
        for (Map<String, Object> data : list) {
            AuthGroup group = new AuthGroup((Integer) data.get("id"), (String) data.get("name"));
            result.add(group);
        }
        return result;
    }

    public Integer findAuthUserIdByUsername(String username) {
        Map<String, Object> map =
        /*JdbcUtils.queryForMap(
        newDbConnection(),
        "SELECT `id` FROM `auth_user` WHERE `username` = '" + username + "'");*/
                hueJdbcTemplate.queryForMap(
                        "SELECT `id` FROM `auth_user` WHERE `username` = '" + username + "'");
        return CollectionUtils.isEmpty(map) ? null : (Integer) map.get("id");
    }

    public Integer findAuthGroupIdByAuthGroupName(String authGroupName) {
        Map<String, Object> map =
        /*JdbcUtils.queryForMap(
        newDbConnection(),
        "SELECT `id` FROM `auth_group` WHERE `name` = '" + authGroupName + "'");*/
                hueJdbcTemplate.queryForMap(
                        "SELECT `id` FROM `auth_group` WHERE `name` = '" + authGroupName + "'");
        return CollectionUtils.isEmpty(map) ? null : (Integer) map.get("id");
    }

    @Transactional
    public void createAuthGroupWithDefaultPermissions(String authGroupName) {
        Assert.hasText(authGroupName, "iQuery用户组名称不允许为空");
        List<Map<String, Object>> exist =
        /*JdbcUtils.queryForList(
        newDbConnection(),
        "SELECT `name` FROM `auth_group` WHERE `name` = '" + authGroupName + "'");*/
                hueJdbcTemplate.queryForList(
                        "SELECT `name` FROM `auth_group` WHERE `name` = '" + authGroupName + "'");
        if (!CollectionUtils.isEmpty(exist)) {
            throw new RuntimeException("iQuery新建用户组名称重复");
        }
        // 添加组
    /*JdbcUtils.executeUpdate(
    newDbConnection(), "INSERT INTO `auth_group` (`name`) VALUES ('" + authGroupName + "')");*/
        hueJdbcTemplate.update("INSERT INTO `auth_group` (`name`) VALUES ('" + authGroupName + "')");
        // 组id
        Integer authGroupId = findAuthGroupIdByAuthGroupName(authGroupName);
        if (authGroupId == null) {
            throw new RuntimeException("iQuery用户组名=" + authGroupName + "在auth_group表中不存在");
        }
        // 设置默认权限复制
        String[] codes = iQueryConfiguration.getIQuerydefaultPermissionsCode().split(",");
        if (codes == null || codes.length == 0) {
            throw new RuntimeException("iQuery新建用户组默认权限在配置文件中没有配置");
        }
        StringBuilder sqlPart = new StringBuilder();
        for (int i = 0; i < codes.length; i++) {
            if (i == codes.length - 1) {
                sqlPart.append("'").append(codes[i].trim()).append("'");
            } else {
                sqlPart.append("'").append(codes[i].trim()).append("',");
            }
        }
        List<Map<String, Object>> permissionIds =
        /*JdbcUtils.queryForList(
        newDbConnection(),
        "SELECT `id` FROM `useradmin_huepermission` WHERE CONCAT(`app`, '.', `action`) IN ("
            + sqlPart.toString()
            + ")");*/
                hueJdbcTemplate.queryForList(
                        "SELECT `id` FROM `useradmin_huepermission` WHERE CONCAT(`app`, '.', `action`) IN ("
                                + sqlPart.toString()
                                + ")");
        if (CollectionUtils.isEmpty(permissionIds)) {
            throw new RuntimeException("iQuery新建用户组默认权限对应的id不存在");
        }
        StringBuilder permissionSql =
                new StringBuilder(
                        "INSERT INTO `useradmin_grouppermission` (`group_id`, `hue_permission_id`)VALUES");
        for (int i = 0; i < permissionIds.size(); i++) {
            permissionSql
                    .append("(")
                    .append(authGroupId)
                    .append(",")
                    .append((Integer) permissionIds.get(i).get("id"))
                    .append(")");
            if (i != permissionIds.size() - 1) {
                permissionSql.append(",");
            }
        }
        // 插入
        // JdbcUtils.executeUpdate(newDbConnection(), permissionSql.toString());
        hueJdbcTemplate.update(permissionSql.toString());
    }

    /**
     * 获取HUE默认账户
     *
     * @param username
     * @return
     */
    public Account getDefaultHueAccount(String username) {
    /*Map<String, Object> map =
        JdbcUtils.queryForMap(
            newDbConnection(),
            "SELECT `username`,`password` FROM auth_user WHERE username = '" + username + "'");
    if (CollectionUtils.isEmpty(map)) {
      throw new NullPointerException("HUE账户不存在");
    }*/
        return new Account(username, newDefaultAccountPassword(username));
    }

    // 统一jdbc
  /*private final DbConnection newDbConnection() {
    return new DbConnection(
        hueDb.getDriverClassName(), hueDb.getUrl(), hueDb.getUsername(), hueDb.getPassword());
  }*/

    // 生成默认账户密码
    private final String newDefaultAccountPassword(String username) {
        return username + SecurityUtils.simpleEncrypt(username);
    }

    @Data
    @AllArgsConstructor
    public static class Account {

        private String username;

        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class AuthGroup {

        private Integer id;

        private String name;
    }
}
