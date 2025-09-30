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
