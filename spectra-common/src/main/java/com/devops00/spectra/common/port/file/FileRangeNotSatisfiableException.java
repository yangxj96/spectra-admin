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

package com.devops00.spectra.common.port.file;

import com.devops00.spectra.common.exception.SpectraException;

/**
 * 文件请求范围不可满足（HTTP 416）。
 *
 * <p>用于区分“范围语法非法”和“起点超出资产总长度”。已知总长度时由适配层返回
 * {@code Content-Range: bytes *}{@code /<total>}。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
public class FileRangeNotSatisfiableException extends SpectraException {

    /** 资产总长度；未知时为负数。 */
    private final long totalSize;

    public FileRangeNotSatisfiableException(String message, long totalSize) {
        super(message);
        this.totalSize = totalSize;
    }

    public FileRangeNotSatisfiableException(String message, long totalSize, Throwable cause) {
        super(message, cause);
        this.totalSize = totalSize;
    }

    /**
     * 资产总长度。
     *
     * @return 总长度字节数；未知时返回负数
     */
    public long totalSize() {
        return totalSize;
    }
}
