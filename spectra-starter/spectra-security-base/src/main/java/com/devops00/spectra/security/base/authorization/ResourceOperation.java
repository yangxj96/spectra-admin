/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.security.base.authorization;

/**
 * 资源访问动作。动作必须与 Permission catalog 中的 action 一一对应。
 */
public enum ResourceOperation {
    LIST("read"),
    DETAIL("read"),
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    BATCH("batch"),
    EXPORT("export");

    private final String code;

    ResourceOperation(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
