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

package com.devops00.spectra.framework.web.advice.exception;

import com.devops00.spectra.common.exception.EntityUpdateException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link CommonExceptionAdvice} 版本冲突 409 契约验证。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class CommonExceptionAdviceConflictTest {

    @Test
    void entityUpdateConflictMapsTo409() {
        var response = new MockHttpServletResponse();
        var body = new CommonExceptionAdvice().spectraException(new EntityUpdateException("conflict"), response);

        assertEquals(409, response.getStatus());
        assertEquals("ENTITY_UPDATE_CONFLICT", body.getErrorCode());
    }
}
