package io.github.yangxj96.spectra.core.user.controller;

import cn.dev33.satoken.annotation.SaCheckEL;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yangxj96.spectra.common.annotation.ULog;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.core.user.javabean.from.RoleFrom;
import io.github.yangxj96.spectra.core.user.javabean.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.user.service.AuthorityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限相关操作
 */
@Slf4j
@SaCheckLogin
@RestController
@RequestMapping("/authority")
public class AuthorityController {

    @Resource
    private AuthorityService bindService;

    @ULog("创建权限")
    @PostMapping
    @SaCheckEL("@ss.administrators()")
    public void createdAuthority(@Validated(Verify.Insert.class) @RequestBody RoleFrom params) {
        throw new NotImplementedException("无需实现错误");
    }

    @ULog("删除权限")
    @DeleteMapping("/{id}")
    @SaCheckEL("@ss.administrators()")
    public void deleteAuthority(@PathVariable String id) {
        throw new NotImplementedException("无需实现错误");
    }

    @ULog("修改权限信息")
    @PutMapping
    @SaCheckEL("@ss.administrators()")
    public void modifyAuthority(@Validated(Verify.Update.class) @RequestBody RoleFrom params) {
        throw new NotImplementedException("无需实现错误");
    }

    @ULog("获取权限树列表")
    @GetMapping("/tree")
    public List<AuthorityTreeVO> tree() {
        return bindService.tree();
    }

}
