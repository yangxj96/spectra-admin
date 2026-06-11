package com.devops00.spectra.core.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.javabean.system.entity.Configured;
import org.apache.ibatis.annotations.Mapper;


/// 系统配置Mapper层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-06
@Mapper
public interface ConfiguredMapper extends BaseMapper<Configured> {
}