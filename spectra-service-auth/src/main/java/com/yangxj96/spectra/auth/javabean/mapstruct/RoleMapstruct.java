/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.auth.javabean.mapstruct;


import com.yangxj96.spectra.auth.javabean.entity.Role;
import com.yangxj96.spectra.auth.javabean.vo.RoleVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 角色相关mapstruct
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Mapper(componentModel = "spring")
public interface RoleMapstruct {

    RoleVO toVO(Role role);

    List<RoleVO> toVOs(List<Role> roles);
}
