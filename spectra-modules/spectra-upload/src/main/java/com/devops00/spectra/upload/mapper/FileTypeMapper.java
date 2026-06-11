package com.devops00.spectra.upload.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.upload.javabean.entity.FileType;
import org.apache.ibatis.annotations.Mapper;

/// 文件类型mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/6 15:33
@Mapper
public interface FileTypeMapper extends BaseMapper<FileType> {
}
