package io.github.yangxj96.spectra.core.user.controller;

import cn.dev33.satoken.annotation.SaCheckEL;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.annotation.ULog;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.system.javabean.vo.MenuVO;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleAuthorityFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.user.javabean.from.RolePageFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityVO;
import io.github.yangxj96.spectra.core.user.javabean.vo.RoleVO;
import io.github.yangxj96.spectra.core.user.service.RelRoleAuthorityService;
import io.github.yangxj96.spectra.core.user.service.RelRoleMenuService;
import io.github.yangxj96.spectra.core.user.service.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色操作
 */
@Slf4j
@SaCheckLogin
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService bindService;

    @Resource
    private RelRoleMenuService relRoleMenuService;

    @Resource
    private RelRoleAuthorityService relRoleAuthorityService;

    @ULog("创建角色")
    @PostMapping
    @SaCheckEL("@ss.hasPermission('ROLE:INSERT')")
    public void created(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        bindService.created(params);
    }

    @ULog("删除角色")
    @DeleteMapping("/{id}")
    @SaCheckEL("@ss.hasPermission('ROLE:DELETE')")
    public void delete(@PathVariable String id) {
        try {
            bindService.delete(Long.parseLong(id));
        } catch (NumberFormatException e) {
            log.error("ID转换异常", e);
        }
    }

    @ULog("修改角色")
    @PutMapping
    @SaCheckEL("@ss.hasPermission('ROLE:UPDATE')")
    public void modify(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        bindService.modify(params);
    }

    /* 查询部分 */

    @ULog("分页查询角色列表")
    @GetMapping("/page")
    public IPage<RoleVO> page(PageFrom page, RolePageFrom params) {
        return bindService.page(page, params);
    }

    @ULog("查询角色列表")
    @GetMapping("/list")
    public List<RoleVO> list() {
        return bindService.all();
    }

    /* 关联处理部分 */

    @ULog("获取角色关联的权限列表")
    @GetMapping("/{roleId}/authority")
    public List<AuthorityVO> getRoleRelAuthorityByRoleId(@PathVariable String roleId) {
        try {
            long id = Long.parseLong(roleId);
            return relRoleAuthorityService.get(id);
        } catch (Exception e) {
            log.atError().log("获取角色关联的权限列表出现错误,{}", e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }

    @ULog("获取角色关联的菜单列表")
    @GetMapping("/{roleId}/menu")
    public List<MenuVO> getRoleRelMenuByRoleId(@PathVariable String roleId) {
        try {
            long id = Long.parseLong(roleId);
            return relRoleMenuService.get(id);
        } catch (Exception e) {
            log.atError().log("获取角色关联的菜单列表出现错误,{}", e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }

    @ULog("保存角色关联的权限列表")
    @PostMapping("/{roleId}/authority")
    public void saveRoleRelAuthorityByRoleId(@PathVariable String roleId, @Validated @RequestBody RoleAuthorityFrom from) {
        try {
            long id = Long.parseLong(roleId);
            relRoleAuthorityService.grant(id, from);
        } catch (Exception e) {
            log.atError().log("保存角色关联的权限列表出现错误,{}", e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }

    @ULog("保存角色关联的菜单列表")
    @PostMapping("/{roleId}/menu")
    public void saveRoleRelMenuByRoleId(@PathVariable String roleId, @Validated @RequestBody RoleMenuFrom from) {
        try {
            long id = Long.parseLong(roleId);
            relRoleMenuService.grant(id, from);
        } catch (Exception e) {
            log.atError().log("保存角色关联的菜单列表出现错误,{}", e.getMessage(), e);
            throw new IllegalArgumentException("参数转换失败");
        }
    }


}
