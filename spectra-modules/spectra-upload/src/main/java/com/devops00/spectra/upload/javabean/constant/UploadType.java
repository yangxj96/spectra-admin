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
import lombok.Getter;

/// 上传方式枚举
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/2 11:04
@Getter
public enum UploadType implements IEnum<String> {

    /// 本地上传
    LOCAL("LOCAL", "本地上传"),
    /// S3协议上传
    S3("S3", "S3协议");

    /// 值(存数据库用的)
    private final String value;

    /// 说明(展示用的)
    private final String name;

    UploadType(String value, String name) {
        this.value = value;
        this.name = name;
    }
}
