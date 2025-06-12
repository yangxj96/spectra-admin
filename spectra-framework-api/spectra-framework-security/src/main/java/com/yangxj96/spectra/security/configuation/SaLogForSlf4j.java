package com.yangxj96.spectra.security.configuation;

import cn.dev33.satoken.log.SaLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 重写日志输出
 *
 * @author 杨新杰
 * @since 2025/5/26 18:07
 */
@Slf4j
@Component
public class SaLogForSlf4j implements SaLog {

    @Override
    public void trace(String str, Object... args) {
        log.trace(str, args);
    }

    @Override
    public void debug(String str, Object... args) {
        log.debug(str, args);
    }

    @Override
    public void info(String str, Object... args) {
        log.info(str, args);
    }

    @Override
    public void warn(String str, Object... args) {
        log.warn(str, args);
    }

    @Override
    public void error(String str, Object... args) {
        log.error(str, args);
    }

    @Override
    public void fatal(String str, Object... args) {
        log.error(str, args);
    }

}
