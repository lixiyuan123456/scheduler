package com.naixue.nxdp.model;

import lombok.Data;

/**
 * @author: wangyu @Created by 2018/2/3
 */
@Data
public class RerunSearchParam {

    private Integer id;
    private String beginJobs;
    private Integer jobType;
    private Integer jobLevel;
    private String userId;
    private Integer historyRun;
    private Integer optStatus;
    private Integer jobState;
    private String labels;
    private String kw;
}
