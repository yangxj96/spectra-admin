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

package com.devops00.spectra.upload.controller;

import com.devops00.spectra.core.configure.ulog.annotation.ULog;
import com.devops00.spectra.upload.service.impl.FileUploadFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 文件操作相关控制器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/19
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FileController {

    private final FileUploadFacade bindService;

    /// 文件上传预处理
    @ULog("文件上传预处理")
    @GetMapping("/pre")
    public void pre() {

    }

    /// 小文件直接保存
    ///
    /// @param from 文件直接保存的参数
    @ULog("普通上传")

    @PostMapping("/upload")
    public void upload() {
    }

    /// 上传切片
    ///
    /// @param from 文件分片上传参数
    @ULog("分片上传")
    @PostMapping("/chunk")
    public void chunk() {
    }

}
