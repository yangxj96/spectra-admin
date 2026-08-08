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

package com.devops00.spectra.oa.leave.javabean.converter;

import org.mapstruct.Mapper;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.oa.leave.javabean.entity.LeaveApplication;
import com.devops00.spectra.oa.leave.javabean.vo.LeaveVO;

/// 请假申请 MapStruct 转换器。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Mapper(config = GlobalMapperConfig.class)
public interface LeaveConverter {
    /// 请假申请实体转视图对象。
    LeaveVO toVO(LeaveApplication source);
}
