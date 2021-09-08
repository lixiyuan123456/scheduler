package com.naixue.nxdp.attachment.goshawk.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.naixue.nxdp.attachment.goshawk.model.plugin.BootstrapTreeView;
import com.naixue.nxdp.util.HttpUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.naixue.nxdp.attachment.goshawk.GoshawkCfg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

public class YarnQueueResource {

    public static Queue enhanceQueue(Queue queue, Queue parentQueue) {
        queue.setParentQueue(parentQueue);
        List<Queue> children = queue.getQueues();
        if (!CollectionUtils.isEmpty(children)) {
            queue.setAclSubmitApps(null);
            queue.setAclAdministerApps(null);
            queue.setSchedulingPolicy(null);
            for (Queue child : children) {
                enhanceQueue(child, queue);
            }
        }
        return queue;
    }

    public static final Queue buildRootQueue() {
        Queue rootQueue = new Queue();
        rootQueue.setRoot(true); // 必须 指明是root
        rootQueue.setName("root"); // 必须 名称必须是root
        rootQueue.setAclSubmitApps(" "); // 必须 默认值是空格
        rootQueue.setQueueMaxAMShareDefault("0.5"); // 必须 默认值是0.5
        rootQueue.setUserMaxAppsDefault("5"); // 必须 默认值5
        rootQueue.setAclAdministerApps(null);
        rootQueue.setSchedulingPolicy(null);
        String clusterMetricsJSON = HttpUtils.httpGet(GoshawkCfg.YARN_QUEUE_RESOURCE_TOTAL_QUERY_URL);
        Assert.hasText(
                clusterMetricsJSON, "URL=" + GoshawkCfg.YARN_QUEUE_RESOURCE_TOTAL_QUERY_URL + "返回的内容为空");
        /*ClusterMetrics clusterMetrics = XmlUtils.toBean(clusterMetricsXml, YarnQueueResource.ClusterMetrics.class);*/
        ClusterMetrics clusterMetrics =
                JSON.parseObject(clusterMetricsJSON, YarnQueueResource.ClusterMetrics.class);
        rootQueue.setClusterMetrics(clusterMetrics);
        List<Queue> queues = new ArrayList<>();
        Queue defaultQueue = new Queue();
        defaultQueue.setName("default");
        defaultQueue.setMinResources("1mb,1vcores");
        defaultQueue.setMaxResources("1mb,1vcores");
        defaultQueue.setAclSubmitApps(null);
        defaultQueue.setAclAdministerApps(null);
        defaultQueue.setSchedulingPolicy(null);
        queues.add(defaultQueue);
        rootQueue.setQueues(queues);
        return rootQueue;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlRootElement(name = "allocations")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Allocation {

        @XmlElement
        private Queue queue;
    }

    @Data
    public static class ClusterMetrics {

        private Double totalMB;

        private Double totalVirtualCores;

        private ClusterMetrics clusterMetrics;

        public ClusterMetrics(Double totalMB, Double totalVirtualCores) {
            this.totalMB = totalMB;
            this.totalVirtualCores = totalVirtualCores;
        }

        public void setClusterMetrics(ClusterMetrics clusterMetrics) {
            this.clusterMetrics = clusterMetrics;
            if (clusterMetrics != null) {
                this.totalMB = clusterMetrics.getTotalMB();
                this.totalVirtualCores = clusterMetrics.getTotalVirtualCores();
            }
        }
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Queue extends BootstrapTreeView {

        @XmlTransient
        private boolean root;

        @XmlAttribute(name = "name")
        private String name;

        private String weight;

        @JsonIgnore
        @JSONField(serialize = false)
        @XmlTransient
        private Queue parentQueue;

        // @XmlTransient private ClusterMetrics parentClusterMetrics;

        @XmlTransient
        private ClusterMetrics clusterMetrics;

        // @XmlTransient private Integer percentValue; // 占比

        @XmlTransient
        private Integer minPercentValue; // 最小占比

        @XmlTransient
        private Integer maxPercentValue; // 最大占比

        private String minResources;

        // @XmlTransient private Double minResourcesValue;

        private String maxResources;

        // @XmlTransient private Double maxResourcesValue;

        private String aclSubmitApps;

        private String aclAdministerApps;

        private String schedulingPolicy = "fair";

        private String queueMaxAMShareDefault;

        private String userMaxAppsDefault;

        @XmlElement(name = "queue")
        private List<Queue> queues;

        public void setName(String name) {
            this.name = name;
            super.setText(name);
            this.aclSubmitApps = name;
            this.aclAdministerApps = name;
        }

        public void setQueues(List<Queue> queues) {
            this.queues = queues;
            if (!CollectionUtils.isEmpty(queues)) {
                List<BootstrapTreeView> nodes = new ArrayList<>();
                for (Queue queue : queues) {
                    nodes.add(queue);
                }
                super.setNodes(nodes);
            }
        }

    /*public void setMaxPercentValue(Integer maxPercentValue) {
      this.maxPercentValue = maxPercentValue;
      if (maxPercentValue != null && !this.name.equals("root") && !this.name.equals("default")) {
        if (this.parentClusterMetrics != null) {
          Double totalMB = this.parentClusterMetrics.getTotalMB();
          Double totalVirtualCores = this.parentClusterMetrics.getTotalVirtualCores();
          Double thisTotalMB = totalMB * maxPercentValue / 100;
          Double thisTotalVirtualCores = totalVirtualCores * maxPercentValue / 100;
          this.maxResources = thisTotalMB + "mb" + "," + thisTotalVirtualCores + "vcores";
          this.clusterMetrics = new ClusterMetrics(thisTotalMB, thisTotalVirtualCores);
        }
      }
    }*/

    /*public void setMinPercentValue(Integer minPercentValue) {
      this.minPercentValue = minPercentValue;
      if (minPercentValue != null && !this.name.equals("root") && !this.name.equals("default")) {
        if (this.parentClusterMetrics != null) {
          Double totalMB = this.parentClusterMetrics.getTotalMB();
          Double totalVirtualCores = this.parentClusterMetrics.getTotalVirtualCores();
          Double thisTotalMB = totalMB * minPercentValue / 100;
          Double thisTotalVirtualCores = totalVirtualCores * minPercentValue / 100;
          this.maxResources = thisTotalMB + "mb" + "," + thisTotalVirtualCores + "vcores";
        }
      }
    }*/

        public void setParentQueue(Queue parentQueue) {
            this.parentQueue = parentQueue;
            if (parentQueue != null && parentQueue.getClusterMetrics() != null) {
                Double totalMB = parentQueue.getClusterMetrics().getTotalMB();
                Double totalVirtualCores = parentQueue.getClusterMetrics().getTotalVirtualCores();
                if (minPercentValue != null && minPercentValue != null) {
                    setMinResources(
                            (int) Math.floor(totalMB * minPercentValue / 100)
                                    + "mb"
                                    + ","
                                    + (int) Math.floor(totalVirtualCores * minPercentValue / 100)
                                    + "vcores");
                    setMaxResources(
                            (int) Math.floor(totalMB * maxPercentValue / 100)
                                    + "mb"
                                    + ","
                                    + (int) Math.floor(totalVirtualCores * maxPercentValue / 100)
                                    + "vcores");
                    setClusterMetrics(
                            new ClusterMetrics(
                                    Math.floor(totalMB * maxPercentValue / 100),
                                    Math.floor(totalVirtualCores * maxPercentValue / 100)));
                }
            }
        }

        @XmlTransient
        @Override
        public List<BootstrapTreeView> getNodes() {
            return super.getNodes();
        }

        @XmlTransient
        @Override
        public boolean isSelectable() {
            return super.isSelectable();
        }

        @XmlTransient
        @Override
        public State getState() {
            return super.getState();
        }

        @XmlTransient
        @Override
        public String getText() {
            return super.getText();
        }
    }
}
