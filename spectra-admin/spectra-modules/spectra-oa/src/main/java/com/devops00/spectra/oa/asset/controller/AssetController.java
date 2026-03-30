package com.devops00.spectra.oa.asset.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.configure.ulog.annotation.ULog;
import com.devops00.spectra.oa.asset.javabean.entity.Asset;
import com.devops00.spectra.oa.asset.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 资产主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:20
@RestController
@RequestMapping("/oa/asset")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService bindService;

    @ULog("分页查询资产")
    @GetMapping("/page")
    public IPage<Asset> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
