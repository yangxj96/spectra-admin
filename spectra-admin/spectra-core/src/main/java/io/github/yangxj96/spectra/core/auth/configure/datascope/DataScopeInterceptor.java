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

        log.atDebug().log("进入拦截: {}", boundSql.getSql());
    }

    /**
     * 判断是否需要应用数据范围过滤
     */
    private boolean shouldApplyDataScopeFilter(DataScopeInfo info) {
        return Boolean.TRUE.equals(info.getFilter()) &&
                !AuthScope.ALL.equals(info.getScope());
    }


}
