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

package com.devops00.spectra.oa.notice.javabean.converter;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.notice.javabean.entity.Notice;
import com.devops00.spectra.oa.notice.javabean.from.NoticeCreateFrom;
import com.devops00.spectra.oa.notice.javabean.vo.NoticeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// 公告对象转换器。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NoticeConverter {

    /// 公告实体转响应视图。
    @Mapping(target = "read", ignore = true)
    @Mapping(target = "readAt", ignore = true)
    NoticeVO toVO(Notice source);

    /// 公告创建入参转实体。
    Notice toEntity(NoticeCreateFrom source);
}
