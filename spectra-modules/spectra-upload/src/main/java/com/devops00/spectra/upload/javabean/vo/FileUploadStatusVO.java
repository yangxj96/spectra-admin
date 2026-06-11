package com.devops00.spectra.upload.javabean.vo;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

/// 文件上传状态
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/1 16:50
@Getter
@Setter
public class FileUploadStatusVO {

    /// 文件上传状态
    ///
    ///  INIT → UPLOADING → DONE → EXPIRED
    private String status;

    /// 已上传分片
    private List<Integer> uploadedChunks;

    /// 总分片数量
    private Integer totalChunks;

    /// 分片大小
    private Long chunkSize;

    /// 完成状态
    private Boolean completed;

}
