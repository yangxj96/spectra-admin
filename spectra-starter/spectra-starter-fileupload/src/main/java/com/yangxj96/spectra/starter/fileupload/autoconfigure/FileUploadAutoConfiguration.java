package com.yangxj96.spectra.starter.fileupload.autoconfigure;

import com.yangxj96.spectra.starter.fileupload.configure.FileType;
import com.yangxj96.spectra.starter.fileupload.configure.FileTypeValidator;
import com.yangxj96.spectra.starter.fileupload.properties.FileUploadProperties;
import com.yangxj96.spectra.starter.fileupload.strategy.FileTypeValidationStrategy;
import com.yangxj96.spectra.starter.fileupload.strategy.impl.ExtensionValidationStrategy;
import com.yangxj96.spectra.starter.fileupload.strategy.impl.MagicNumberValidationStrategy;
import com.yangxj96.spectra.starter.fileupload.strategy.impl.MimeValidationStrategy;
import com.yangxj96.spectra.starter.fileupload.strategy.impl.TikaValidationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传类型验证需要的相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-06-27
 */
@Slf4j
@EnableConfigurationProperties(FileUploadProperties.class)
public class FileUploadAutoConfiguration {

    private static final String PREFIX = "[FileUpload]:";

    private final FileUploadProperties properties;

    public FileUploadAutoConfiguration(FileUploadProperties properties) {
        this.properties = properties;
    }

    /**
     * 文件类型验证策略管理器
     *
     * @return {@link FileTypeValidator}
     */
    @Bean
    public FileTypeValidator fileTypeValidator() {
        log.atDebug().log(PREFIX + "载入文件类型验证策略管理器");
        List<FileTypeValidationStrategy> strategies = new ArrayList<>();
        List<FileType> allowedTypes = properties.getAllowedTypes();
        // 根据配置添加策略处理器
        for (var strategy : properties.getStrategies()) {
            if (strategy.isAssignableFrom(MimeValidationStrategy.class)) {
                strategies.add(new MimeValidationStrategy(mimes(allowedTypes)));
            }
            if (strategy.isAssignableFrom(ExtensionValidationStrategy.class)) {
                strategies.add(new ExtensionValidationStrategy(allowedTypes));
            }
            if (strategy.isAssignableFrom(MagicNumberValidationStrategy.class)) {
                strategies.add(new MagicNumberValidationStrategy(allowedTypes));
            }
            if (strategy.isAssignableFrom(TikaValidationStrategy.class)) {
                strategies.add(new TikaValidationStrategy(mimes(allowedTypes)));
            }
        }
        return new FileTypeValidator(strategies);
    }


    /**
     * 获取可上传的文件的mimes列表
     *
     * @param allowedTypes 允许上传的列表
     * @return mime列表
     */
    private List<String> mimes(List<FileType> allowedTypes) {
        List<String> m = new ArrayList<>();
        for (FileType allowedType : allowedTypes) {
            m.add(allowedType.getMime());
        }
        return m;
    }

}
