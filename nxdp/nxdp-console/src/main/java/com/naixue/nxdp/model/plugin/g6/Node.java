package com.naixue.nxdp.model.plugin.g6;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class Node {

    private String id;

    @Builder.Default
    private int x = 0;

    @Builder.Default
    private int y = 0;

    private String color;

    private String shape;

    private String label;

    private Style style;

    @Builder.Default
    private int size = 50;

    private int nodeType;

    @Builder
    @Getter
    @Setter
    @ToString
    public static class Style {

        private String fill;

        private String stroke;
    }
}
