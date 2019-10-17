package com.hbl.camera.option;

public final class Option<T> {
    public String getId() {
        return id;
    }

    public Class<T> getValueClass() {
        return valueClass;
    }

    private String id;
    private Class<T> valueClass;

    private Option(String id, Class<T> valueClass) {
        this.id = id;
        this.valueClass = valueClass;
    }

    public static <T> Option<T> create(String id, Class<T> valueClass) {
        return new Option<>(id, valueClass);
    }
}
