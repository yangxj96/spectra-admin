package com.devops00.spectra.core.javabean.system.converter;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.core.configure.mapstruct.TimeMapper;
import com.devops00.spectra.core.javabean.system.entity.Configured;
import com.devops00.spectra.core.javabean.system.vo.ConfiguredVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/// 系统配置Mapstruct
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/11/06
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface ConfiguredConverter {

    /// 数据库实体转VO
    ///
    /// @param source 数据库实体
    /// @return VO
    ConfiguredVO toVO(Configured source);

    /// 转换到分页的VO信息
    ///
    /// @param source 分页信息
    /// @return IPAGE
    @Mapping(target = "pages", ignore = true)
    Page<ConfiguredVO> toVOPage(Page<Configured> source);

}
