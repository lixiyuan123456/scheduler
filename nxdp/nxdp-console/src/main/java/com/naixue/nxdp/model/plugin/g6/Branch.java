package com.naixue.nxdp.model.plugin.g6;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class Branch {

    private String id;

    private String label;

    private List<Branch> children;
}
