package com.devops00.spectra.core.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.javabean.system.from.ConfiguredFrom;
import com.devops00.spectra.core.javabean.system.from.ConfiguredPageFrom;
import com.devops00.spectra.core.javabean.system.vo.ConfiguredVO;
import com.devops00.spectra.core.service.system.ConfiguredService;
import com.devops00.spectra.log.base.annotation.ULog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/// 系统配置控制器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@RestController
@RequestMapping("/configured")
public class ConfiguredController {

    private final ConfiguredService bindService;

    public ConfiguredController(ConfiguredService bindService) {
        this.bindService = bindService;
    }

    /**
     * 修改系统配置<br/>
     * <b>只能修改值和说明</b>
     *
     * @param params 修改参数入参实体
     */
    @ULog("'修改系统配置'")
    @PutMapping
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void modify(@Validated @RequestBody ConfiguredFrom params) {
        bindService.modify(params);
    }

    @ULog("'分页查询系统配置'")
    @GetMapping("/page")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params) {
        return bindService.page(page, params);
    }

}
