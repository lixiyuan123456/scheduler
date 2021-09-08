package com.naixue.nxdp.attachment.goshawk.service;

import org.springframework.data.domain.Page;

import com.naixue.nxdp.attachment.goshawk.model.Whitelist;

/**
 * @author 刘蒙
 */
public interface IYarnWhitelistService {

    Page<Whitelist> list(Whitelist condition, Integer pageIndex, Integer pageSize);
}
