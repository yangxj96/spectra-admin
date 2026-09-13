/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.oa.file.reference;

/**
 * OA 文件业务引用类型。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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
