package com.devops00.spectra.upload.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.upload.javabean.entity.FileUploadChunk;
import org.apache.ibatis.annotations.Mapper;

/// 文件分片信息Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:06
@Mapper
public interface FileUploadChunkMapper extends BaseMapper<FileUploadChunk> {

}
