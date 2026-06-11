package com.devops00.spectra.upload.javabean.vo;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/// 文件上传-预处理-VO
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/1 16:42
@Getter
@Setter
public class FileUploadPreVO {

    /// 文件是否已经存在，存在就不在继续了
    private boolean exists;

    /// 文件ID
    private UUID fileId;

    /// 是否需要分片
    private boolean multipart;

    /// 文件上传ID
    private String uploadId;

    /// 分片大小
    private long chunkSize;

}
