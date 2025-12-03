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

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.common.base.javabean.from.PageFrom;
import io.github.yangxj96.spectra.core.configure.ulog.annotation.ULog;
import io.github.yangxj96.spectra.core.javabean.user.from.UserPageFrom;
import io.github.yangxj96.spectra.core.javabean.user.from.UserSaveFrom;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserOnlineVO;
import io.github.yangxj96.spectra.core.javabean.user.vo.UserPageVO;
import io.github.yangxj96.spectra.core.service.user.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService bindService;

    @ULog("创建用户")
    @PostMapping
    public void created(@Validated(Verify.Insert.class) @RequestBody UserSaveFrom params) {
        bindService.create(params);
    }

    @ULog("根据ID删除用户")
    @DeleteMapping("/{uid}")
    public void deleteById(@PathVariable String uid) {
        bindService.deleteById(uid);
    }

    @ULog("根据ID更新用户信息")
    @PutMapping
    public void updateById(@Validated(Verify.Update.class) @RequestBody UserSaveFrom params) {
        bindService.updateById(params);
    }

    @ULog("重置用户密码")
    @PutMapping("/password/reset/{uid}")
    public void passwordResetById(@PathVariable String uid) {
        bindService.passwordResetById(uid);
    }

    @ULog("分页查询用户列表")
    @GetMapping("/page")
    public IPage<UserPageVO> page(PageFrom page, UserPageFrom params) {
        return bindService.page(page, params);
    }

    @ULog("获取在线用户")
    @GetMapping("/online")
    public IPage<UserOnlineVO> online(PageFrom page) {
        return bindService.online(page);
    }
}
