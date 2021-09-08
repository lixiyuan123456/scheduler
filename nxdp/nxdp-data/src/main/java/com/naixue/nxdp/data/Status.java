package com.naixue.nxdp.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Status {
    DELETED(0, "删除"),
    NORMAL(1, "正常");

    @Getter
    private final Integer code;
    @Getter
    private final String name;

    public static Status parseEnum(final Integer code) {
        for (Status status : Status.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getCode() + "";
    }
}
