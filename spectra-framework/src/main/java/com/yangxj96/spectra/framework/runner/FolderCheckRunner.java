package com.yangxj96.spectra.framework.runner;

import com.yangxj96.spectra.common.properties.SystemProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 文件夹检查
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Component
public class FolderCheckRunner implements ApplicationRunner {

    @Resource
    private SystemProperties properties;

    @Override
    public void run(ApplicationArguments args) {
        File folder = new File(properties.getBaseDir());
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                log.atDebug().log("已创建文件夹: " + folder.getAbsolutePath());
            } else {
                log.atDebug().log("无法创建文件夹: " + folder.getAbsolutePath());
            }
        } else {
            log.atDebug().log("文件夹已存在: " + folder.getAbsolutePath());
        }
    }
}
