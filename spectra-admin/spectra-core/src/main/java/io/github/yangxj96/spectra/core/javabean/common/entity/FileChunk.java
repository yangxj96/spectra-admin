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
 * 文件分片信息实体
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
@TableName(value = "SYS_FILE_CHUNK")
public class FileChunk extends BaseEntity implements Serializable {

    /**
     * 文件名
     */
    @TableField(value = "FILE_NAME")
    private String fileName;

    /**
     * 文件唯一标识（如 SHA256 或 UUID）
     */
    @TableField(value = "FILE_ID")
    private String fileId;

    /**
     * 分片序号（从 0 开始）
     */
    @TableField(value = "CHUNK_INDEX")
    private Integer chunkIndex;

    /**
     * 总分片数（冗余，便于校验）
     */
    @TableField(value = "TOTAL_CHUNKS")
    private Integer totalChunks;

    /**
     * 分片在磁盘/OSS 的存储路径或 Key
     */
    @TableField(value = "CHUNK_PATH")
    private String chunkPath;

    /**
     * 当前分片字节数
     */
    @TableField(value = "CHUNK_SIZE")
    private Long chunkSize;

}
