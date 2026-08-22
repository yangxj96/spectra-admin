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

import com.devops00.spectra.core.user.javabean.from.UserSaveFrom;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.service.UserService;
import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户接口单元测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/11 17:49
 */
@Slf4j
@Disabled("手工管理员数据初始化脚本，不应由常规测试自动修改开发数据库")
@SpringBootTest
class UserControllerTest {

    @Resource
    private UserService userService;

    @Test
    void initAdminUser() {
        // 最小化新建用户
        UserSaveFrom from = new UserSaveFrom();
        from.setStatus(UserStatus.DISABLED);
        from.setEmployeeNo("ADMIN");
        from.setRealName("系统管理员");
        from.setPhone("13800000000");
        from.setEmail("admin@devops00.com");
        from.setDepartmentId(UuidCreator.getTimeOrderedEpoch());

        var createdUser = userService.create(from);

        Assertions.assertNotNull(createdUser, "创建用户应返回用户信息");
        Assertions.assertNotNull(createdUser.getId(), "创建用户应返回用户 ID");
    }
}
