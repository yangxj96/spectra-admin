package com.devops00.spectra.upload.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import org.apache.ibatis.annotations.Param;
import org.jspecify.annotations.Nullable;

/// 文件信息Mapper
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/8 00:06
public interface FileInfoMapper extends BaseMapper<FileInfo> {

    /**
     * 根据hash查找文件
     *
     * @param hash hash
     * @return 如果文件存在则返回文件,否则返回null
     */
    @Nullable FileInfo getByHash(@Param("hash") String hash);
}
