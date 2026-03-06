package com.devops00.spectra.upload.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.*;

import java.io.Serializable;

/// 文件分片信息实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:03
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "file_chunk")
public class FileChunk extends BaseEntity implements Serializable {

    /// 文件名
    @TableField(value = "file_name")
    private String fileName;

    /// 文件唯一标识（如 SHA256 或 UUID）
    @TableField(value = "file_id")
    private String fileId;

    /// 分片序号（从 0 开始）
    @TableField(value = "chunk_index")
    private Integer chunkIndex;

    /// 总分片数（冗余，便于校验）
    @TableField(value = "total_chunks")
    private Integer totalChunks;

    /// 分片在磁盘/OSS 的存储路径或 Key
    @TableField(value = "chunk_path")
    private String chunkPath;

    /// 当前分片字节数
    @TableField(value = "chunk_size")
    private Long chunkSize;

}
