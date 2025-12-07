package io.github.yangxj96.spectra.core.javabean.common.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.github.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * 文件信息实体
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/8 00:03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "SYS_FILE_INFO")
public class FileInfo extends BaseEntity implements Serializable {

    /**
     * 生成的32位的文件名称
     */
    @TableField(value = "FILE_NAME")
    private String fileName;

    /**
     * 文件源名称
     */
    @TableField(value = "ORIGIN_NAME")
    private String originName;

    /**
     * 文件后缀
     */
    @TableField(value = "SUFFIX")
    private String suffix;

    /**
     * 文件存储位置
     */
    @TableField(value = "PATH")
    private String path;

    /**
     * 文件大小
     */
    @TableField(value = "SIZE")
    private Long size;

    /**
     * 文件hash值
     */
    @TableField(value = "HASH")
    private String hash;

    /**
     * 文件存储类型
     */
    @TableField(value = "STORAGE_TYPE")
    private Short storageType;

}
