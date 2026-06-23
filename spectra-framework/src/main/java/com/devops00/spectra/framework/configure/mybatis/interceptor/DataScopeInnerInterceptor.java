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

package com.devops00.spectra.framework.configure.mybatis.interceptor;


import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;

import java.util.UUID;

/// MP执行的单表SQL拦截处理
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/5/9 17:35
@Slf4j
@RequiredArgsConstructor
public class DataScopeInnerInterceptor implements MultiDataPermissionHandler {

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        UUID userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        // TODO 方向已定,还需完善
        return null;
    }


}
