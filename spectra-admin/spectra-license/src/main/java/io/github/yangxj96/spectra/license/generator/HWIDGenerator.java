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

package io.github.yangxj96.spectra.license.generator;

import io.github.yangxj96.spectra.license.utils.HardwareIdUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 硬件ID生成
 */
@Slf4j
public class HWIDGenerator {

    private HWIDGenerator() {
    }


    static void main() {
        // bf15a08c8ec82a4d399437c98e0f7dfe10f6a252da8189e7824d2a3506522c42
        String string = HardwareIdUtil.generateHWID();
        log.atDebug().log("当前硬件ID:{}", string);
    }

}
