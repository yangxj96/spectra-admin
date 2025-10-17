/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.yangxj96.spectra.core.javabean.user.entity.RelUserRole;

/**
 * 角色关联权限中间表
 */
public interface RelUserRoleMapper extends BaseMapper<RelUserRole> {
}
