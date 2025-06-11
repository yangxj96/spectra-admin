/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.kernel.controller;

import com.yangxj96.spectra.kernel.entity.dto.Menu;
import com.yangxj96.spectra.kernel.service.MenuService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

/**
 * 菜单接口测试
 *
 * @author 杨新杰
 * @since 2025/6/3 23:44
 */
@SpringBootTest
class MenuControllerTest {

    @Resource
    private MenuService menuService;

    // 插入父级
    @Test
    void insertMenu() {
        var parent = Arrays.asList(
                // 1929928379575111682
                Menu.builder()
                        .pid(0L)
                        .icon("icon-setting")
                        .name("系统管理")
                        .path("/system")
                        .component("layout")
                        .layout("layout")
                        .build(),
                // 1929928379667386370
                Menu.builder()
                        .pid(0L)
                        .icon("icon-setting")
                        .name("组件示例")
                        .path("/example")
                        .component("layout")
                        .layout("layout")
                        .build()
        );
        var f = menuService.saveBatch(parent);
        Assertions.assertTrue(f, "插入失败");
    }

    // 插入子级
    @Test
    void insertMenu2() {
        var parent = Arrays.asList(
                // 系统管理下级
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("用户管理").path("user")
                        .component("/System/User/index").build(),
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("角色管理").path("role")
                        .component("/System/Role/index").build(),
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("权限管理").path("power")
                        .component("/System/Power/index").build(),
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("字典管理").path("dict")
                        .component("/System/Dict/index").build(),
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("定时任务").path("task")
                        .component("/System/Task/index").build(),
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("菜单管理").path("menu")
                        .component("/System/Menu/index").build(),
                Menu.builder().pid(1929928379575111682L)
                        .icon("icon-setting-role").name("文件存储").path("storage")
                        .component("/System/Storage/index").build(),
                // 组件示例下级
                Menu.builder().pid(1929928379667386370L)
                        .icon("icon-setting-role").name("图表示例").path("echarts")
                        .component("/Example/Echarts/index").build(),
                Menu.builder().pid(1929928379667386370L)
                        .icon("icon-setting-role").name("列表示例").path("table")
                        .component("/Example/Table/index").build(),
                Menu.builder().pid(1929928379667386370L)
                        .icon("icon-setting-role").name("表单示例").path("form")
                        .component("/Example/Form/index").build()
        );
        var f = menuService.saveBatch(parent);
        Assertions.assertTrue(f, "插入失败");
    }

}
