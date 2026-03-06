package com.devops00.spectra.upload.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.*;

import java.io.Serializable;

/// 文件信息实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:03
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_info")
public class FileInfo extends BaseEntity implements Serializable {

    /// 生成的32位的文件名称
    @TableField(value = "file_name")
    private String fileName;

    /// 文件源名称
    @TableField(value = "origin_name")
    private String originName;

    /// 文件后缀
    @TableField(value = "suffix")
    private String suffix;

    /// 文件存储位置
    @TableField(value = "path")
    private String path;

    /// 文件大小
    @TableField(value = "size")
    private Long size;

    /// 文件hash值
    @TableField(value = "hash")
    private String hash;

    /// 文件存储类型
    @TableField(value = "storage_type")
    private Short storageType;

}
