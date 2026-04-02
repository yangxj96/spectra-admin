package com.devops00.spectra.upload.javabean.constant;


import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/**
 * 上传方式枚举
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/4/2 11:04
 */
@Getter
public enum UploadType implements IEnum<String> {
    /// 本地上传
    LOCAL("LOCAL", "本地上传"),
    /// S3协议上传
    S3("S3", "S3协议");

    /// 值(存数据库用的)
    private final String value;

    /// 说明(展示用的)
    private final String name;

    UploadType(String value, String name) {
        this.value = value;
        this.name = name;
    }


}
