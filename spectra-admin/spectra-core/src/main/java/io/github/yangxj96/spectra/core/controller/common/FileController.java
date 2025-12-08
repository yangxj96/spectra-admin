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

package io.github.yangxj96.spectra.core.controller.common;

import io.github.yangxj96.spectra.core.javabean.common.from.FileChunkFrom;
import io.github.yangxj96.spectra.core.javabean.common.from.FilePreprocessFrom;
import io.github.yangxj96.spectra.core.javabean.common.from.FileUploadFrom;
import io.github.yangxj96.spectra.core.javabean.common.vo.FilePreprocessVO;
import io.github.yangxj96.spectra.core.service.common.FileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * <p>
 * 文件操作相关控制器
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService bindService;

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
    public void chunk(FileChunkFrom from) throws IOException {
        bindService.chunk(from);
    }

    /**
     * 查询文件上传进度
     *
     * @param md5 文件MD5
     */
    @GetMapping("/progress")
    public void progress(String md5) {

    }

}
