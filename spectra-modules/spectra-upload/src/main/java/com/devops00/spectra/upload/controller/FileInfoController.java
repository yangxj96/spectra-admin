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

package com.devops00.spectra.upload.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.upload.javabean.from.FilePageFrom;
import com.devops00.spectra.upload.javabean.vo.FileInfoVO;
import com.devops00.spectra.upload.service.FileInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/// 文件信息管理控制器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/4 16:00
@Slf4j
@RestController
@RequestMapping("/file/info")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ROLE_DEV_OPS')")
public class FileInfoController {

    private final FileInfoService fileInfoService;

    /// 分页查询文件列表
    @ULog("'分页查询文件列表'")
    @GetMapping(value = "/page", version = "1.0.0+")
    public IPage<FileInfoVO> page(PageFrom page, FilePageFrom params) {
        return fileInfoService.page(page, params);
    }

    /// 删除文件(软删除)
    @ULog("'删除文件'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    public void deleteById(@PathVariable UUID id) {
        fileInfoService.deleteById(id);
    }

}
