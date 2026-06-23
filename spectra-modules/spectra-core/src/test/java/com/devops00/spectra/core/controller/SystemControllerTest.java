/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import com.devops00.spectra.core.system.javabean.entity.DictItem;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.service.*;
import com.devops00.spectra.core.user.javabean.entity.Authority;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.javabean.from.RoleAuthorityFrom;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.service.AuthorityService;
import com.devops00.spectra.core.user.service.RelRoleAuthorityService;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import com.devops00.spectra.core.user.service.RoleService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/// 系统接口单元测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/11 17:49
@SpringBootTest
class SystemControllerTest {

    @Resource
    private AuthorityService authorityService;

    @Resource
    private RoleService roleService;

    @Resource
    private MenuService menuService;

    @Resource
    private DictGroupService dictGroupService;

    @Resource
    private DictItemService dictItemService;

    @Resource
    private DepartmentService departmentService;

    @Resource
    private SysConfigService sysConfigService;

    @Resource
    private RelRoleAuthorityService relRoleAuthorityService;

    @Resource
    private RelRoleMenuService relRoleMenuService;

    /// 初始化权限
    @Test
    void initAuthority() {
        var root = new Authority();
        root.setName("顶级权限");
        root.setCode("*");
        authorityService.save(root);

        List<Authority> children = List.of(
                newAuthority("菜单权限", "MENU:*", root.getId()),
                newAuthority("字典管理", "DICT:*", root.getId()),
                newAuthority("部门管理", "DEPT:*", root.getId()),
                newAuthority("用户管理", "USER:*", root.getId())
        );

        authorityService.saveBatch(children);

        // 3️⃣ 子权限的操作权限（第二层子节点）
        Map<String, Authority> map = children.stream()
                .collect(Collectors.toMap(Authority::getName, Function.identity()));

        List<Authority> ops = List.of(
                newAuthority("菜单新增", "MENU:INSERT", map.get("菜单权限").getId()),
                newAuthority("菜单修改", "MENU:UPDATE", map.get("菜单权限").getId()),
                newAuthority("菜单删除", "MENU:DELETE", map.get("菜单权限").getId()),

                newAuthority("字典新增", "DICT:INSERT", map.get("字典管理").getId()),
                newAuthority("字典删除", "DICT:DELETE", map.get("字典管理").getId()),
                newAuthority("字典修改", "DICT:UPDATE", map.get("字典管理").getId()),

                newAuthority("部门新增", "DEPT:INSERT", map.get("部门管理").getId()),
                newAuthority("部门删除", "DEPT:DELETE", map.get("部门管理").getId()),
                newAuthority("部门修改", "DEPT:UPDATE", map.get("部门管理").getId()),

                newAuthority("用户新增", "USER:INSERT", map.get("用户管理").getId()),
                newAuthority("用户删除", "USER:DELETE", map.get("用户管理").getId()),
                newAuthority("用户修改", "USER:UPDATE", map.get("用户管理").getId())
        );

        authorityService.saveBatch(ops);
    }

    /// 初始化角色
    @Test
    void initRoles() {
        var roles = List.of(
                newRole("运维管理员", "ROLE_DEV_OPS"),
                newRole("系统管理员", "ROLE_ADMIN_SYSTEM"),
                newRole("用户", "ROLE_USER"),
                newRole("审计员", "ROLE_AUDIT")
        );
        roleService.saveBatch(roles);
    }

    /// 角色关联权限
    @Test
    void initRelRoleAuthority() {
        var p = new RoleAuthorityFrom();
        p.setRoleId(UUID.fromString("019bdfad-ded6-731e-b27f-c4e7ca7b0d9d"));
        p.setAuthorityIds(List.of(UUID.fromString("019bdf8f-6542-7b9b-8fc7-2eae5b1a4c94")));
        relRoleAuthorityService.grant(UUID.fromString("019bdfad-ded6-731e-b27f-c4e7ca7b0d9d"), p);
    }

    /// 初始化菜单
    @Test
    void initMenus() {
        var roots = List.of(
                newMenu(null, "首页", "icon-home", "/", "layout", "blank", 0),
                newMenu(null, "工作台", "icon-setting", "/workbench", "layout", "blank", 1),
                newMenu(null, "系统监控", "icon-setting", "/monitor", "layout", "default", 2),
                newMenu(null, "组件示例", "icon-setting", "/exampl", "layout", "default", 3),
                newMenu(null, "系统管理", "icon-setting", "/system", "layout", "default", 4)
        );
        menuService.saveBatch(roots);

        Map<String, Menu> map = roots.stream()
                .collect(Collectors.toMap(Menu::getName, Function.identity()));

        var ops = List.of(
                // 首页
                newMenu(map.get("首页").getId(), "首页默认", "icon-module", "", "/Home/index", null, 0),
                // 工作台
                newMenu(map.get("工作台").getId(), "工作台默认页面", "icon-module", "", "/Workbench/index", null, 0),
                // 系统监控
                newMenu(map.get("系统监控").getId(), "服务监控", "icon-module", "server", "/Monitor/Server/index", null, 0),
                newMenu(map.get("系统监控").getId(), "在线用户", "icon-module", "online", "/Monitor/Online/index", null, 0),
                newMenu(map.get("系统监控").getId(), "定时任务", "icon-module", "task", "/Monitor/Task/index", null, 0),
                newMenu(map.get("系统监控").getId(), "缓存监控", "icon-module", "cache", "/Monitor/Cache/index", null, 0),
                newMenu(map.get("系统监控").getId(), "数据监控", "icon-module", "database", "/Monitor/Database/index", null, 0),
                // 组件示例
                newMenu(map.get("组件示例").getId(), "列表示例", "icon-module", "table", "/Example/Table/index", null, 0),
                newMenu(map.get("组件示例").getId(), "Markdown", "icon-module", "markdown", "/Example/Markdown/index", null, 0),
                newMenu(map.get("组件示例").getId(), "表单示例", "icon-module", "form", "/Example/Form/index", null, 0),
                newMenu(map.get("组件示例").getId(), "图表示例", "icon-module", "echarts", "/Example/Echarts/index", null, 0),
                // 系统管理
                newMenu(map.get("系统管理").getId(), "文件存储", "icon-module", "storage", "/System/Storage/index", null, 0),
                newMenu(map.get("系统管理").getId(), "用户管理", "icon-module", "user", "/System/User/index", null, 0),
                newMenu(map.get("系统管理").getId(), "系统配置", "icon-module", "configured", "System/Configured/index", null, 0),
                newMenu(map.get("系统管理").getId(), "流程管理", "icon-module", "workflow", "/System/Workflow/index", null, 0),
                newMenu(map.get("系统管理").getId(), "部门管理", "icon-module", "dept", "/System/Dept/index", null, 0),
                newMenu(map.get("系统管理").getId(), "字典管理", "icon-module", "dict", "/System/Dict/index", null, 0),
                newMenu(map.get("系统管理").getId(), "许可管理", "icon-module", "license", "/System/License/index", null, 0),
                newMenu(map.get("系统管理").getId(), "菜单管理", "icon-module", "menu", "/System/Menu/index", null, 0),
                newMenu(map.get("系统管理").getId(), "访问控制", "icon-module", "RBAC", "/System/RBAC/index", null, 0)
        );

        menuService.saveBatch(ops);
    }

    @Test
    void initRelRoleMenu() {
        var p = new RoleMenuFrom();
        p.setRoleId(UUID.fromString("019bdfad-ded6-731e-b27f-c4e7ca7b0d9d"));
        p.setMenuIds(List.of(
                UUID.fromString("019bdfc5-b220-7bd9-80d1-1a1db193c151"),
                UUID.fromString("019bdfc5-b31f-7020-b678-35fae63c432c"),
                UUID.fromString("019bdfc5-b328-7de0-9e8c-2ac0cc51969e"),
                UUID.fromString("019bdfc5-b32a-7c31-bbff-3992be5fff64"),
                UUID.fromString("019bdfc5-b32c-74e9-90ac-0540954c4e4a"),
                UUID.fromString("019bdfc5-b347-75c0-bcac-98ed9e44cf93"),
                UUID.fromString("019bdfc5-b34b-7619-8f37-b052e64e4e27"),
                UUID.fromString("019bdfc5-b34d-74fd-8ad8-f2f7976634d1"),
                UUID.fromString("019bdfc5-b350-7168-84d6-ffaaf874b6fc"),
                UUID.fromString("019bdfc5-b352-7d24-b5af-8d0a0042a4f9"),
                UUID.fromString("019bdfc5-b355-701e-99f2-7012b17490de"),
                UUID.fromString("019bdfc5-b358-7794-adf1-b5cc9a3d5883"),
                UUID.fromString("019bdfc5-b35a-7d3d-bf74-f903a795d7cd"),
                UUID.fromString("019bdfc5-b35c-76f0-b622-34e11d75dd27"),
                UUID.fromString("019bdfc5-b35d-7a55-b441-38f77a88036a"),
                UUID.fromString("019bdfc5-b35e-71a2-9b71-c5ab8900f08f"),
                UUID.fromString("019bdfc5-b35f-74b6-abe3-3816db511129"),
                UUID.fromString("019bdfc5-b362-70a0-8cbd-53f96e96c64c"),
                UUID.fromString("019bdfc5-b363-750f-8cd2-010b659463a8"),
                UUID.fromString("019bdfc5-b365-7a12-b646-3a5c922bd6f9"),
                UUID.fromString("019bdfc5-b367-7e26-9c50-620660e13019"),
                UUID.fromString("019bdfc5-b36a-7700-b8c3-7c251f1f79a2"),
                UUID.fromString("019bdfc5-b36d-72df-b7a7-6c82d9199988"),
                UUID.fromString("019bdfc5-b36f-7e70-a79d-09c5facdf296"),
                UUID.fromString("019bdfc5-b370-70ca-a33c-25044878eeda")

        ));
        relRoleMenuService.grant(UUID.fromString("019bdfad-ded6-731e-b27f-c4e7ca7b0d9d"), p);
    }

    @Test
    void initDictGroup() {
        var roots = List.of(
                newDictGroup(null, "系统配置", "sys"),
                newDictGroup(null, "OA相关", "oa")
        );

        dictGroupService.saveBatch(roots);

        Map<String, DictGroup> map = roots.stream()
                .collect(Collectors.toMap(DictGroup::getName, Function.identity()));

        var ops = List.of(
                // OA相关
                newDictGroup(map.get("OA相关").getId(), "流程分类", "dict_workflow_type"),
                // 系统配置
                newDictGroup(map.get("系统配置").getId(), "用户状态", "sys_user_state"),
                newDictGroup(map.get("系统配置").getId(), "通用状态", "sys_common_state"),
                newDictGroup(map.get("系统配置").getId(), "组织机构类型", "sys_organization_type"),
                newDictGroup(map.get("系统配置").getId(), "用户性别", "sys_user_gender"),
                newDictGroup(map.get("系统配置").getId(), "时区", "sys_timezone"),
                newDictGroup(map.get("系统配置").getId(), "语言", "sys_language"),
                newDictGroup(map.get("系统配置").getId(), "邮箱后缀", "sys_email_suffix"),
                newDictGroup(map.get("系统配置").getId(), "水印类型", "sys_watermark")
        );

        dictGroupService.saveBatch(ops);
    }

    @Test
    void initDictItem() {
        var groupMap = dictGroupService.list()
                .stream()
                .collect(Collectors.toMap(DictGroup::getName, e -> e));
        var ops = List.of(
                // 流程分类
                newDictItem(groupMap.get("流程分类").getId(), "财务", "0"),
                newDictItem(groupMap.get("流程分类").getId(), "人事", "1"),
                // 用户状态
                newDictItem(groupMap.get("用户状态").getId(), "正常", "0"),
                newDictItem(groupMap.get("用户状态").getId(), "冻结", "1"),
                newDictItem(groupMap.get("用户状态").getId(), "封禁", "2"),
                // 通用状态
                newDictItem(groupMap.get("通用状态").getId(), "启用", "0"),
                newDictItem(groupMap.get("通用状态").getId(), "禁用", "1"),
                // 组织机构类型
                newDictItem(groupMap.get("组织机构类型").getId(), "系统运维", "0"),
                newDictItem(groupMap.get("组织机构类型").getId(), "集团总部", "1"),
                newDictItem(groupMap.get("组织机构类型").getId(), "省级公司", "2"),
                newDictItem(groupMap.get("组织机构类型").getId(), "市级公司", "3"),
                newDictItem(groupMap.get("组织机构类型").getId(), "县级公司", "4"),
                newDictItem(groupMap.get("组织机构类型").getId(), "部门", "5"),
                newDictItem(groupMap.get("组织机构类型").getId(), "科室/小组", "6"),
                // 用户性别
                newDictItem(groupMap.get("用户性别").getId(), "未知", "1"),
                newDictItem(groupMap.get("用户性别").getId(), "男性", "2"),
                newDictItem(groupMap.get("用户性别").getId(), "女性", "3"),
                newDictItem(groupMap.get("用户性别").getId(), "人妖", "4"),
                newDictItem(groupMap.get("用户性别").getId(), "沃尔玛塑料袋", "5"),
                // 时区
                newDictItem(groupMap.get("时区").getId(), "国际日期变更线西", "Etc/GMT+12"),
                newDictItem(groupMap.get("时区").getId(), "萨摩亚时间", "Pacific/Pago_Pago"),
                newDictItem(groupMap.get("时区").getId(), "夏威夷时间", "Pacific/Honolulu"),
                newDictItem(groupMap.get("时区").getId(), "阿拉斯加时间", "America/Anchorage"),
                newDictItem(groupMap.get("时区").getId(), "美国太平洋时间", "America/Los_Angeles"),
                newDictItem(groupMap.get("时区").getId(), "美国山地时间", "America/Denver"),
                newDictItem(groupMap.get("时区").getId(), "美国中部时间", "America/Chicago"),
                newDictItem(groupMap.get("时区").getId(), "美国东部时间", "America/New_York"),
                newDictItem(groupMap.get("时区").getId(), "大西洋时间", "America/Halifax"),
                newDictItem(groupMap.get("时区").getId(), "巴西时间（圣保罗）", "America/Sao_Paulo"),
                newDictItem(groupMap.get("时区").getId(), "亚速尔群岛时间", "Atlantic/Azores"),
                newDictItem(groupMap.get("时区").getId(), "协调世界时", "UTC"),
                newDictItem(groupMap.get("时区").getId(), "中欧时间（柏林）", "Europe/Berlin"),
                newDictItem(groupMap.get("时区").getId(), "东欧时间（雅典）", "Europe/Athens"),
                newDictItem(groupMap.get("时区").getId(), "莫斯科时间", "Europe/Moscow"),
                newDictItem(groupMap.get("时区").getId(), "印度标准时间", "Asia/Kolkata"),
                newDictItem(groupMap.get("时区").getId(), "中国标准时间(北京时间)", "Asia/Shanghai"),
                newDictItem(groupMap.get("时区").getId(), "日本标准时间", "Asia/Tokyo"),
                newDictItem(groupMap.get("时区").getId(), "澳大利亚东部时间", "Australia/Sydney"),
                // 语言
                newDictItem(groupMap.get("语言").getId(), "中文（简体）", "zh-CN"),
                newDictItem(groupMap.get("语言").getId(), "中文（繁体）", "zh-TW"),
                newDictItem(groupMap.get("语言").getId(), "英语", "en"),
                newDictItem(groupMap.get("语言").getId(), "日语", "ja"),
                newDictItem(groupMap.get("语言").getId(), "韩语", "ko"),
                newDictItem(groupMap.get("语言").getId(), "法语", "fr"),
                newDictItem(groupMap.get("语言").getId(), "德语", "de"),
                newDictItem(groupMap.get("语言").getId(), "西班牙语", "es"),
                newDictItem(groupMap.get("语言").getId(), "俄语", "ru"),
                newDictItem(groupMap.get("语言").getId(), "葡萄牙语", "pt"),
                newDictItem(groupMap.get("语言").getId(), "意大利语", "it"),
                newDictItem(groupMap.get("语言").getId(), "阿拉伯语", "ar"),
                newDictItem(groupMap.get("语言").getId(), "印地语", "hi"),
                // 邮箱后缀
                newDictItem(groupMap.get("邮箱后缀").getId(), "devops", "devops00.com"),
                newDictItem(groupMap.get("邮箱后缀").getId(), "谷歌邮箱", "gmail.com"),
                newDictItem(groupMap.get("邮箱后缀").getId(), "QQ邮箱", "qq.com"),
                newDictItem(groupMap.get("邮箱后缀").getId(), "微软hotmail", "hotmail.com"),
                // 水印类型
                newDictItem(groupMap.get("水印类型").getId(), "系统生成", "1"),
                newDictItem(groupMap.get("水印类型").getId(), "固定值", "2")
        );

        for (DictItem op : ops) {
            if (op.getGid() != null) {
                System.out.println(op);
                throw new NullPointerException("找不到");
            }
        }

        dictItemService.saveBatch(ops);

    }

    @Test
    void updateOrgPath() {
        var organizations = departmentService.list();
        for (Department department : organizations) {
            var path = departmentService.generatePath(department.getId());
            System.out.println("路由:" + path);
            department.setPath(path);
        }
        departmentService.updateBatchById(organizations);
    }

    /// 测试UUIv7查询速度
    @Test
    void queryConfig() {
        System.out.println("总条目数:" + sysConfigService.count());
        StopWatch watch = new StopWatch("UserQuery");

        watch.start("第一页,每页100条查询");
        sysConfigService.page(new Page<>(1, 100));
        watch.stop();

        watch.start("第九十九页,每页100条查询");
        sysConfigService.page(new Page<>(99, 100));
        watch.stop();

        watch.start("第三百九十八也页,每页32455条查询");
        sysConfigService.page(new Page<>(398, 325));
        watch.stop();

        System.out.println(watch.prettyPrint());
    }

    private DictItem newDictItem(UUID gid, String label, String value) {
        var datum = new DictItem();
        datum.setGid(gid);
        datum.setLabel(label);
        datum.setValue(value);
        datum.setSort((short) 0);
        datum.setState((short) 0);
        return datum;
    }

    private DictGroup newDictGroup(UUID pid, String name, String code) {
        var datum = new DictGroup();
        datum.setPid(pid);
        datum.setName(name);
        datum.setCode(code);
        datum.setState(Boolean.TRUE);
        return datum;
    }

    private Menu newMenu(UUID pid, String name, String icon, String path, String component, String layout, Integer sort) {
        var menu = new Menu();
        menu.setPid(pid);
        menu.setName(name);
        menu.setIcon(icon);
        menu.setPath(path);
        menu.setComponent(component);
        menu.setLayout(layout);
        menu.setSort(sort);
        return menu;
    }

    private Role newRole(String name, String code) {
        var role = new Role();
        role.setName(name);
        role.setCode(code);
        role.setState(Boolean.TRUE);
        //role.setScope(DataScopeType.ALL);
        role.setBuiltin(Boolean.TRUE);
        return role;
    }

    private Authority newAuthority(String name, String code, UUID pid) {
        Authority auth = new Authority();
        auth.setName(name);
        auth.setCode(code);
        auth.setPid(pid);
        return auth;
    }

}