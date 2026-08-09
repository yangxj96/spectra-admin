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

package com.devops00.spectra.oa.application.javabean.constant;

/**
 * 通用 OA 申请状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public enum ApplicationStatus {
    /**
     * 草稿。
     */
    DRAFT,

    /**
     * 审批中。
     */
    IN_REVIEW,

    /**
     * 已通过。
     */
    APPROVED,

    /**
     * 已驳回。
     */
    REJECTED,

    /**
     * 已撤回。
     */
    WITHDRAWN,

    /**
     * 已取消。
     */
    CANCELLED
}
