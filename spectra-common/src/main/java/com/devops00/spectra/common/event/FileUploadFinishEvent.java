package com.devops00.spectra.common.event;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/// 文件上传完成事件
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/6/15 17:21
@Getter
public class FileUploadFinishEvent extends ApplicationEvent {

    private final UUID fileId;

    public FileUploadFinishEvent(Object source, UUID fileId) {
        super(source);
        this.fileId = fileId;
    }

}
