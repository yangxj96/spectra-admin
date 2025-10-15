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

package io.github.yangxj96.spectra.core.auth.configure.datascope;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import io.github.yangxj96.spectra.common.enums.AuthScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

/**
 * 数据范围拦截器
 */
@Slf4j
@Component
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 只处理 SELECT 请求
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }

        var dataScopeInfo = DataScopeContext.get();
        if (dataScopeInfo == null || !shouldApplyDataScopeFilter(dataScopeInfo)) {
            return;
        }

        log.debug("进入拦截: {}", boundSql.getSql());
    }

    /**
     * 判断是否需要应用数据范围过滤
     */
    private boolean shouldApplyDataScopeFilter(DataScopeInfo info) {
        return Boolean.TRUE.equals(info.getFilter()) &&
                !AuthScope.ALL.equals(info.getScope());
    }


}
