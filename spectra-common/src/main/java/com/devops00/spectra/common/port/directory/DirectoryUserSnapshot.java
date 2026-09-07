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

package com.devops00.spectra.common.port.directory;

import java.util.UUID;

/**
 * 跨模块目录查询返回的用户快照。
 *
 * @param id           用户 ID
 * @param employeeNo   工号
 * @param displayName  显示名称
 * @param username     登录用户名
 * @param status       生命周期状态名称
 * @param departmentId 主部门 ID
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
public record DirectoryUserSnapshot(
                                    UUID id,
                                    String employeeNo,
                                    String displayName,
                                    String username,
                                    String status,
                                    UUID departmentId) {
}
