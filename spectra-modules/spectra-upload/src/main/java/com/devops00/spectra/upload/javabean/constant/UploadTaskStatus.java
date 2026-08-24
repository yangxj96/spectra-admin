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

package com.devops00.spectra.upload.javabean.constant;

import com.baomidou.mybatisplus.annotation.IEnum;

/**
 * 文件上传任务状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
public enum UploadTaskStatus implements IEnum<String> {

    /** 初始化。 */
    INIT("INIT"),
    /** 上传中。 */
    UPLOADING("UPLOADING"),
    /** 合并中。 */
    MERGING("MERGING"),
    /** 上传完成。 */
    DONE("DONE"),
    /** 上传失败。 */
    FAILED("FAILED"),
    /** 任务已过期。 */
    EXPIRED("EXPIRED");

    private final String value;

    UploadTaskStatus(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
