package com.naixue.nxdp.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.naixue.nxdp.dao.MetadataLabelRepository;
import com.naixue.nxdp.model.User;
import com.naixue.nxdp.model.metadata.MetadataLabel;

@Service
public class MetadataLabelService {

    @Autowired
    private MetadataLabelRepository metadataLebelRepository;

    public List<MetadataLabel> findAll(Integer parentId, Integer level) {
        return metadataLebelRepository.findByParentIdAndLevelAndStatus(parentId, level, 1);
    }

    public List<MetadataLabel> findAllFirstLevel() {
        return metadataLebelRepository.findByParentIdAndLevelAndStatus(0, 1, 1);
    }

    @Transactional
    public MetadataLabel save(User u, MetadataLabel lebel) {
        if (lebel.getCreatorId() == null) {
            lebel.setCreatorId(u.getId());
            lebel.setCreateTime(new Date());
        }
        lebel.setModifierId(u.getId());
        lebel.setModifyTime(new Date());
        lebel.setStatus(1);
        return metadataLebelRepository.save(lebel);
    }

    @Transactional
    public void delete(User u, Integer id) {
        MetadataLabel lebel = metadataLebelRepository.findOne(id);
        if (lebel == null) {
            return;
        }
        lebel.setStatus(0);
        metadataLebelRepository.save(lebel);
        if (lebel.getLevel() == 1) {
            List<MetadataLabel> children = metadataLebelRepository.findByParentIdAndLevelAndStatus(lebel.getId(), 2, 1);
            if (CollectionUtils.isEmpty(children)) {
                return;
            }
            for (MetadataLabel child : children) {
                child.setStatus(0);
            }
            metadataLebelRepository.save(children);
        }
    }
}
