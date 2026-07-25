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

package com.devops00.spectra.upload.service;


import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.upload.javabean.entity.FileType;

import java.util.List;
import java.util.Set;

/// 文件类型服务
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/6 15:32
public interface FileTypeService extends BaseService<FileType> {

    /// 查询所有允许上传的扩展名集合（白名单）
    ///
    /// @return 允许的扩展名集合（小写，含点号，如 .jpg）
    Set<String> findAllowedExtensions();

    /// 查询所有危险类型的扩展名集合（黑名单）
    ///
    /// @return 危险的扩展名集合（小写，含点号，如 .exe）
    Set<String> findDangerousExtensions();

    /// 查询所有允许上传的 MIME 类型集合（白名单）
    ///
    /// @return 允许的 MIME 集合（小写）
    Set<String> findAllowedMimes();

    /// 查询所有危险类型的 MIME 类型集合（黑名单）
    ///
    /// @return 危险的 MIME 集合（小写）
    Set<String> findDangerousMimes();

    /// 查询所有危险类型中含魔数规则的记录
    ///
    /// @return 含 magicRules 的危险类型列表
    List<FileType> findDangerousWithMagicRules();

}
