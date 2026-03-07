package com.devops00.spectra.upload.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.handler.StringArrayTypeHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/// 文件类型表
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/6 15:27
@Getter
@Setter
@ToString
@TableName(value = "file_type", autoResultMap = true)
public class FileType extends BaseEntity {

    /// 文件类型名称
    @TableField("name")
    private String name;

    /// 文件后缀（.jpg .png 等）
    @TableField(value = "extension", typeHandler = StringArrayTypeHandler.class)
    private List<String> extension;

    /// MIME 类型
    @TableField(value = "mime", typeHandler = StringArrayTypeHandler.class)
    private List<String> mime;

    /// 文件魔数
    @TableField("magic_number")
    private byte[] magicNumber;

    /// 魔数偏移
    @TableField("magic_offset")
    private Integer magicOffset;

    /// 最大文件大小（bytes）
    @TableField("max_size")
    private Long maxSize;

    /// 是否允许预览
    @TableField("previewable")
    private Boolean previewable;

    /// 是否允许上传
    @TableField("allowed_upload")
    private Boolean allowedUpload;

    /// 是否危险类型
    @TableField("dangerous")
    private Boolean dangerous;

    /// 备注
    @TableField("remark")
    private String remark;
}
