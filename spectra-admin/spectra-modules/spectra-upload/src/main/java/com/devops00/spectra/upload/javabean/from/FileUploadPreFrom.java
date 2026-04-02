package com.devops00.spectra.upload.javabean.from;


import lombok.Getter;
import lombok.Setter;

/// 文件上传预处理
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/7 22:59
@Getter
@Setter
public class FileUploadPreFrom {

    private String filename;

    private Long size;

    private String hash;

}
