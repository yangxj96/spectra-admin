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

package com.devops00.spectra.security.starter.filter;


import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/// token鉴权过滤器
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/2 23:45
@NullMarked
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String token = SecUtil.getCurrentToken();
        if (StrUtils.isNotBlank(token)) {
            SecurityUser user = SecUtil.getCurrentUser(token);
            if (user == null) {
                SecurityContextHolder.clearContext();
            } else {
                var auth = new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
                // 活跃续期
                SecUtil.refreshToken(token);
            }
        }
        chain.doFilter(request, response);
    }

}
