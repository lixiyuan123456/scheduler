package com.naixue.nxdp.attachment.goshawk.dao;

import com.naixue.nxdp.attachment.goshawk.model.Whitelist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 苍蝇系统-yarn管理-白名单
 *
 * @author 刘蒙
 */
public interface YarnWhiltelistRepository
        extends JpaRepository<Whitelist, Integer>, JpaSpecificationExecutor<Whitelist> {
}
