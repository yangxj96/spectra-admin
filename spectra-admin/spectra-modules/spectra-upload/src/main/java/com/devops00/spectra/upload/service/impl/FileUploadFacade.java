package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.upload.javabean.from.FileChunkFrom;
import com.devops00.spectra.upload.javabean.from.FilePreprocessFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.vo.FilePreprocessVO;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.service.FileUploadService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

/// 文件上传工厂层
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/31 14:34
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadFacade {

    /// Spring 应用上下文，用于按类型动态获取 Bean
    private final ApplicationContext applicationContext;

    /// 文件上传配置（用于指定具体实现类）
    private final FileUploadProperties properties;

    /// 实际绑定的文件上传实现（策略实现），在初始化阶段确定，后续直接复用
    private FileUploadService delegate;

    /// 初始化文件上传实现
    ///
    /// <p>逻辑说明：
    /// <ul>
    ///     <li>优先使用配置中指定的实现类（properties.impl）</li>
    ///     <li>如果未配置，则默认使用本地实现（FileUploadServiceLocalImpl）</li>
    ///     <li>通过 Spring 容器获取对应 Bean，保证生命周期与依赖注入正常</li>
    /// </ul>
    ///
    /// <p>异常处理：
    /// <ul>
    ///     <li>如果指定的实现类未注册为 Bean，会在此处初始化失败</li>
    /// </ul>
    @PostConstruct
    public void init() {
        Class<? extends FileUploadService> clazz = properties.getImpl();

        try {
            Class<? extends FileUploadService> targetClass =
                    (clazz != null) ? clazz : FileUploadServiceLocalImpl.class;
            delegate = applicationContext.getBean(targetClass);
            log.info("{}FileUploadService 使用实现: {}", LogPrefix.STORAGE.p(), delegate.getClass().getSimpleName());
        } catch (Exception e) {
            throw new IllegalStateException("初始化 FileUploadService 失败", e);
        }
    }

    public FilePreprocessVO preprocess(FilePreprocessFrom from) {
        return delegate.preprocess(from);
    }

    public void upload(FileUploadFrom from) {
        delegate.upload(from);
    }

    public void chunk(FileChunkFrom from) {
        delegate.chunk(from);
    }
}