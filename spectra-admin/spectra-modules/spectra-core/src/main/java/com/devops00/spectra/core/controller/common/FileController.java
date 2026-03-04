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

package com.devops00.spectra.core.controller.common;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.javabean.common.from.FileChunkFrom;
import com.devops00.spectra.core.javabean.common.from.FilePreprocessFrom;
import com.devops00.spectra.core.javabean.common.from.FileUploadFrom;
import com.devops00.spectra.core.javabean.common.vo.FilePreprocessVO;
import com.devops00.spectra.core.service.common.FileService;
import lombok.extern.slf4j.Slf4j;
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
public class FileController {

    private final FileService bindService;

    public FileController(FileService bindService) {
        this.bindService = bindService;
    }

    /**
     * 预处理文件
     */
    @GetMapping("/preprocess")
    public FilePreprocessVO preprocess(FilePreprocessFrom from) {
        return bindService.preprocess(from);
    }

    /**
     * 小文件直接保存
     *
     * @param from 文件直接保存的参数
     */
    @PostMapping("/upload")
    public void upload(FileUploadFrom from) {
        bindService.upload(from);
    }

    /**
     * 上传切片
     *
     * @param from 文件分片上传参数
     */
    @PostMapping("/chunk")
    public void chunk(FileChunkFrom from) {
        bindService.chunk(from);
    }

    /**
     * 查询文件上传进度
     *
     * @param md5 文件MD5
     */
    @GetMapping("/progress")
    public void progress(String md5) {
        // 暂时未实现
        log.debug("{}入参:{}", LogPrefix.STORAGE, md5);
    }

}
