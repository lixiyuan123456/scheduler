package com.naixue.nxdp.attachment.goshawk.model.plugin;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class BootstrapTreeView {

    @XmlTransient
    private String text;

    @XmlTransient
    private boolean selectable = true;

    @XmlTransient
    private State state = new State(false, false, false, false);

    @XmlTransient
    private List<BootstrapTreeView> nodes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class State {

        private boolean checked;

        private boolean disabled;

        private boolean expanded;

        private boolean selected;
    }
}
