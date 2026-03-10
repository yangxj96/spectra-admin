package com.devops00.spectra.upload.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.upload.javabean.entity.FileChunk;
import org.apache.ibatis.annotations.Mapper;
import org.jspecify.annotations.Nullable;

import java.util.List;

/// 文件分片信息Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:06
@Mapper
public interface FileChunkMapper extends BaseMapper<FileChunk> {

    /// 根据hash检查之前是否有上传过分片信息
    ///
    /// @param fileId 文件ID,当前以文件hash值作为文件ID
    /// @return 分片信息,可能为null
    @Nullable List<FileChunk> getByFileId(String fileId);

}
