package com.devops00.spectra.upload.javabean.vo;


import lombok.Getter;
import lombok.Setter;

/// 分片上传的上传结果
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/1 16:42
@Getter
@Setter
public class FileUploadChunkVO {


    /// 分片编号
    private int chunkNumber;

    /// 标签 S3 / OSS 必须
    private String etag;

}
