package com.naixue.nxdp.attachment.goshawk.service;

import com.naixue.nxdp.attachment.goshawk.model.QueryCondition;
import com.naixue.nxdp.attachment.goshawk.model.YarnJobSpark;
import org.springframework.data.domain.Page;

import com.naixue.nxdp.attachment.goshawk.model.YarnJobMapReduce;

/**
 * @author 刘蒙
 */
public interface IYarnJobQueryService {

    Page<YarnJobSpark> listSpark(QueryCondition condition, Integer pageIndex, Integer pageSize);

    Page<YarnJobMapReduce> listMapReduce(QueryCondition condition, Integer pageIndex, Integer pageSize);
}
