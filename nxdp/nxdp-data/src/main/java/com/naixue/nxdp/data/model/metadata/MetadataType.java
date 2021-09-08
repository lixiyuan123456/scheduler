package com.naixue.nxdp.data.model.metadata;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MetadataType {
    MYSQL(1, "MYSQL"),
    HIVE(2, "HIVE2");

    @Getter
    private final Integer code;
    @Getter
    private final String name;

    public static MetadataType parseEnum(final Integer code) {
        for (MetadataType type : MetadataType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
