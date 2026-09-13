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

package com.devops00.spectra.core.security.secret.javabean.vo;

import java.util.List;

/**
 * 封装密钥相关的响应数据。
 *
 * @param valid      校验是否通过
 * @param entryCount 密钥条目数量
 * @param entries    待导入或导出的密钥条目集合
 * @param conflicts  导入时发现的冲突条目集合
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record SecretImportPreviewVO(boolean valid, int entryCount, List<Entry> entries, List<String> conflicts) {

    /**
     * 封装条目相关的响应数据。
     *
     * @param code        业务对象的唯一编码
     * @param category    业务类别
     * @param version     当前对象或配置的版本号
     * @param fingerprint 密钥材料的指纹值
     * @param registered  该密钥版本是否已登记
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    public record Entry(String code, String category, int version, String fingerprint, boolean registered) {
    }
}
