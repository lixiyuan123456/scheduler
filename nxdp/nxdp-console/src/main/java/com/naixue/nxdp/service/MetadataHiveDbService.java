package com.naixue.nxdp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.naixue.nxdp.dao.HadoopBindingRepository;
import com.naixue.nxdp.dao.MetadataHiveDbRepository;
import com.naixue.nxdp.model.HadoopBinding;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.metadata.MetadataHiveDb;

@Service
public class MetadataHiveDbService {

    @Autowired
    private HadoopBindingRepository hadoopBindingRepository;

    @Autowired
    private MetadataHiveDbRepository metadataHiveDbRepository;

    // @Autowired private MetadataHiveDbPermissionRepository metadataHiveDbPermissionRepository;

    public List<MetadataHiveDb> getAuthorizedMetadataHiveDbs(User user) {
        Assert.notNull(user, "请求参数不允许为空");
        HadoopBinding hadoopBinding = hadoopBindingRepository.findByDeptId(user.getDeptId());
        if (hadoopBinding == null) {
            throw new RuntimeException("当前用户username=" + user.getName() + "所在的部门没有配置hadoop用户组");
        }
        return hadoopBinding.getMetadataHiveDbs();
    }

    public MetadataHiveDb getById(Integer id) {
        return metadataHiveDbRepository.findOne(id);
    }

    public List<MetadataHiveDb> listDbs() {
        return metadataHiveDbRepository.findAll();
    }
}
