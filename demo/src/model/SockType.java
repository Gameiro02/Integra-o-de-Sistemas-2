package com.hood.model;

public enum SockType {
    INVISIBLE, LOW_CUT, OVER_THE_CALF;

    public static SockType fromString(String type) throws IllegalArgumentException {
        for (SockType sockType : SockType.values()) {
            if (sockType.name().equalsIgnoreCase(type)) {
                return sockType;
            }
        }
        throw new IllegalArgumentException("Invalid sock type: " + type);
    }
}
