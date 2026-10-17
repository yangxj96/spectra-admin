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

package com.devops00.spectra.core.upload.controller;

import com.devops00.spectra.common.port.file.FileRangeNotSatisfiableException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link FileUploadExceptionAdvice} 的 416 范围契约验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class FileUploadExceptionAdviceTest {

    private final FileUploadExceptionAdvice advice = new FileUploadExceptionAdvice();

    @Test
    void rangeNotSatisfiableCarriesTotalLength() {
        var response = advice.handleRangeNotSatisfiable(new FileRangeNotSatisfiableException("范围越界", 100L));
        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatusCode());
        assertEquals("bytes */100", response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
        assertEquals("FILE_RANGE_NOT_SATISFIABLE", response.getBody().getErrorCode());
    }

    @Test
    void unknownTotalOmitsContentRange() {
        var response = advice.handleRangeNotSatisfiable(new FileRangeNotSatisfiableException("范围无效", -1L));
        assertEquals(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, response.getStatusCode());
        assertNull(response.getHeaders().getFirst(HttpHeaders.CONTENT_RANGE));
    }
}
