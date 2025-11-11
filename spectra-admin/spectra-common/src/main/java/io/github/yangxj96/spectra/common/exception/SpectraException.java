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

package io.github.yangxj96.spectra.common.exception;

import java.io.Serial;

/**
 * 光谱平台基础异常
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
public class SpectraException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SpectraException() {
        super();
    }

    public SpectraException(String message) {
        super(message);
    }

    public SpectraException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpectraException(Throwable cause) {
        super(cause);
    }

    protected SpectraException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
