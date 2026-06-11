package com.devops00.spectra.core.service.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.javabean.system.entity.Configured;
import com.devops00.spectra.core.javabean.system.from.ConfiguredFrom;
import com.devops00.spectra.core.javabean.system.from.ConfiguredPageFrom;
import com.devops00.spectra.core.javabean.system.vo.ConfiguredVO;

/// 系统配置Service层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-06
public interface ConfiguredService extends BaseService<Configured> {

    /// 修改系统配置的值和说明
    ///
    /// @param params 修改入参
    void modify(ConfiguredFrom params);


    /// 分页查询系统配置项
    ///
    /// @param page   分页信息
    /// @param params 过滤参数
    /// @return 分页响应信息
    IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params);
}
