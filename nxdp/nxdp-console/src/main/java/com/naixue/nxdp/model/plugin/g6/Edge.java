package com.naixue.nxdp.model.plugin.g6;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class Edge {

    private String id;

    private String source;

    private String target;

    @Builder.Default
    private String shape = Shape.line.name();

    @Builder.Default
    private Style style = Style.builder().endArrow(true).build();

    @ToString
    public static enum Shape {
        line,
        polyline,
        spline,
        quadratic,
        cubic;
    }

    @Builder
    @Getter
    @Setter
    @ToString
    public static class Style {

        private boolean startArrow;

        private boolean endArrow;

        private String stroke;
    }
}
