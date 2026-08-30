/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.oa.support;

/** OA 文件业务引用类型。 */
public enum OaFileReferenceType {
    APPLICATION_ATTACHMENT("OA_APPLICATION_ATTACHMENT"),
    CONTRACT_VERSION("OA_CONTRACT_VERSION"),
    DOCUMENT_VERSION("OA_DOCUMENT_VERSION"),
    REIMBURSEMENT_ATTACHMENT("OA_REIMBURSEMENT_ATTACHMENT");

    private final String value;

    OaFileReferenceType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
