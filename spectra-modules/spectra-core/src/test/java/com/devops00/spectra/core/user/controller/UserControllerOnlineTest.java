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

package com.devops00.spectra.core.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.user.javabean.from.OnlineUserPageFrom;
import com.devops00.spectra.core.user.javabean.vo.OnlineUserPageVO;
import com.devops00.spectra.core.user.service.UserOnboardingService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 在线用户分页 API 路由、参数和权限契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class UserControllerOnlineTest {

    @Test
    void shouldBindFiltersAndKeepTheSessionReadPermission() throws Exception {
        UserService userService = mock();
        var controller = new UserController(userService, mock(UserOnboardingService.class),
                mock(SecurityContextAccessor.class));
        PageFrom page = new PageFrom();
        page.setPageNum(2L);
        page.setPageSize(10L);
        OnlineUserPageFrom filter = new OnlineUserPageFrom();
        filter.setUsername("alice");
        filter.setRealName("Alice");
        Page<OnlineUserPageVO> expected = new Page<>(2, 10, 1);
        when(userService.online(page, filter)).thenReturn(expected);

        assertThat(controller.online(page, filter)).isSameAs(expected);
        verify(userService).online(page, filter);

        var endpoint = UserController.class.getMethod("online", PageFrom.class, OnlineUserPageFrom.class);
        assertThat(endpoint.getAnnotation(GetMapping.class).value()).containsExactly("/online");
        assertThat(endpoint.getAnnotation(GetMapping.class).version()).isEqualTo("1.0.0");
        assertThat(endpoint.getAnnotation(PreAuthorize.class).value()).contains("session:read");
        assertThat(endpoint.getAnnotation(Audit.class)).isNotNull();
    }
}
