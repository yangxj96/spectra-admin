/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.controller.user;

import cn.dev33.satoken.annotation.SaCheckEL;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yangxj96.spectra.framework.features.ulog.annotation.ULog;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.core.javabean.user.from.RoleFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.AuthorityTreeVO;
import io.github.yangxj96.spectra.core.service.user.AuthorityService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 权限相关操作
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
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
