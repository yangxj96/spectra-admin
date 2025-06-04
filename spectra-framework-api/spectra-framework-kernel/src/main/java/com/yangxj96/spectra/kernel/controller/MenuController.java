package com.yangxj96.spectra.kernel.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import com.yangxj96.spectra.core.base.Verify;
import com.yangxj96.spectra.core.response.R;
import com.yangxj96.spectra.kernel.entity.from.MenuSaveFrom;
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
    @GetMapping("/tree")
    public R<List<Tree<String>>> tree() {
        return R.success(bindService.tree());
    }

    /**
     * 新增菜单信息
     *
     * @param params 菜单信息
     * @return 修改结果
     */
    @SaCheckPermission("menu::insert")
    @PostMapping("/created")
    public R<Object> created(@Validated(Verify.Insert.class) MenuSaveFrom params) {
        bindService.created(params);
        return R.created();
    }

    /**
     * 修改菜单信息
     *
     * @param params 菜单信息
     * @return 修改结果
     */
    @SaCheckPermission("menu::modify")
    @PutMapping("/modify")
    public R<Object> modify(@Validated(Verify.Update.class) MenuSaveFrom params) {
        bindService.modify(params);
        return R.noContent();
    }
}
