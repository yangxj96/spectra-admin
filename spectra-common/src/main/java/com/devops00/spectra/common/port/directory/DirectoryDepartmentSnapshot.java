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
 * 跨模块目录查询返回的部门快照。
 *
 * @param id       部门 ID
 * @param parentId 父部门 ID
 * @param name     部门名称
 * @param path     部门完整路径
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/7
 */
public record DirectoryDepartmentSnapshot(
                                          UUID id,
                                          UUID parentId,
                                          String name,
                                          String path) {
}
