package com.devops00.spectra.upload.javabean.from;


import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/// 文件分片上传参数
///
/// @param file     分片文件
/// @param fileName 文件名称
/// @param hash     整体文件的hash
/// @param count    分片数量
/// @param index    分片索引
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/7 23:18
@Getter
@Setter
public class FileUploadChunkFrom {

    /// 需要上传的文件
    private MultipartFile file;

    /// 上传ID
    private String uploadId;

    /// 文件名称
    private String fileName;

    /// hash值
    private String hash;

    /// 总分片数
    private Integer count;

    /// 当前idx
    private Integer index;

}
