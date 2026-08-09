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

package com.devops00.spectra.oa.contact.javabean.converter;

import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.contact.javabean.vo.ContactVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 通讯录对象转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface ContactConverter {

    /**
     * 用户实体转通讯录视图，部门名称由 Service 根据批量查询结果补充。
     */
    @Mapping(target = "departmentName", ignore = true)
    ContactVO toVO(User source);
}
