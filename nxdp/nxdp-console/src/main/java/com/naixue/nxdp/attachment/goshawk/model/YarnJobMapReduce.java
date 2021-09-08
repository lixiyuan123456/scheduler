package com.naixue.nxdp.attachment.goshawk.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author 刘蒙
 */
@Data
@Entity
@Table(name = "t_cluster_yarn_mapreduce")
public class YarnJobMapReduce {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "job_id")
    private String jobId;

    @Column(name = "job_name")
    private String jobName;

    @Column(name = "author_name")
    private String authorName;

    @Column(name = "map_task_number")
    private Integer mapTaskNumber;

    @Column(name = "reduce_task_number")
    private Integer reduceTaskNumber;

    @Column(name = "execute_time")
    private Long executeTime;

    @Column(name = "start_time")
    private Date startTime;

    @Column(name = "finish_time")
    private Date finishTime;

    @Column(name = "app_type")
    private String appType;

    @Column(name = "app_schedule_type")
    private String appScheduleType;

    @Column(name = "insert_time")
    private Date insertTime;

    @Column(name = "queue_name")
    private String queueName;

    @Column(name = "user_name")
    private String userName;
}
