package com.yangxj96.spectra.kernel.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yangxj96.spectra.core.entity.from.PageFrom;
import com.yangxj96.spectra.core.response.R;
import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.service.MenuService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单控制器
 *
 * @author 杨新杰
 * @since 2025/6/3 23:18
 */
@RestController
@RequestMapping("/menu")
public class MenuController {

    @Resource
    private MenuService bindService;

    /**
     * 分页查询菜单信息
     */
    @SaCheckLogin
    @GetMapping("/page")
    public R<IPage<Menu>> page(PageFrom page) {
        return R.success(bindService.page(page));
    }

    /**
     * 获取树形菜单
     *
     * @return 构建的树形菜单
     */
    @SaCheckLogin
    @GetMapping("/tree")
    public R<List<Tree<String>>> tree() {
        return R.success(bindService.tree());
    }
}
