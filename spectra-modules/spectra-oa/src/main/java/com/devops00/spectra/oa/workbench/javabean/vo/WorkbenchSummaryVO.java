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

package com.devops00.spectra.oa.workbench.javabean.vo;

import lombok.Data;

/// OA 工作台摘要。
@Data
public class WorkbenchSummaryVO {
    private long todoCount;
    private long unreadNotificationCount;
    private long draftCount;
    private long inReviewCount;
    private long approvedCount;
    private long rejectedCount;
}
