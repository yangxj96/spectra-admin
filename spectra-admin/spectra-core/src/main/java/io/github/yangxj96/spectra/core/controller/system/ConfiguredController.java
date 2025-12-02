package io.github.yangxj96.spectra.core.controller.system;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.javabean.system.from.ConfiguredFrom;
import io.github.yangxj96.spectra.core.javabean.system.from.ConfiguredPageFrom;
import io.github.yangxj96.spectra.core.javabean.system.vo.ConfiguredVO;
import io.github.yangxj96.spectra.core.service.system.ConfiguredService;
import io.github.yangxj96.spectra.framework.features.ulog.annotation.ULog;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 系统配置控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@RestController
@RequestMapping("/configured")
public class ConfiguredController {

    @Resource
    private ConfiguredService bindService;

    /**
     * 修改系统配置<br/>
     * <b>只能修改值和说明</b>
     *
     * @param params 修改参数入参实体
     */
    @ULog("修改系统配置")
    @PutMapping
    public void modify(@Validated @RequestBody ConfiguredFrom params) {
        bindService.modify(params);
    }

    @ULog("分页查询系统配置")
    @GetMapping("/page")
    public IPage<ConfiguredVO> page(PageFrom page, ConfiguredPageFrom params) {
        return bindService.page(page, params);
    }

}
