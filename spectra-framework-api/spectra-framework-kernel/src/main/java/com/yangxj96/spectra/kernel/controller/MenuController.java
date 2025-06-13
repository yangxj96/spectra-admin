package com.yangxj96.spectra.kernel.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.core.base.Verify;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
import com.yangxj96.spectra.kernel.entity.vo.MenuTreeVO;
import com.yangxj96.spectra.kernel.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
 */
@SaCheckLogin
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService bindService;

    /**
     * 获取树形菜单
     *
     * @return 构建的树形菜单
     */
    @ULog(value = "获取树形菜单")
    @GetMapping("/tree")
    public List<MenuTreeVO> tree() {
        return bindService.tree();
    }

    /**
     * 新增菜单信息
     *
     * @param params 菜单信息
     */
    @ULog("新增菜单")
    @PostMapping("/created")
    public void created(@Validated(Verify.Insert.class) @RequestBody MenuSaveFrom params) {
        bindService.created(params);
    }

    /**
     * 修改菜单信息
     *
     * @param params 菜单信息
     */
    @ULog("修改菜单")
    @PutMapping("/modify")
    public void modify(@Validated(Verify.Update.class) @RequestBody MenuSaveFrom params) {
        bindService.modify(params);
    }
}
