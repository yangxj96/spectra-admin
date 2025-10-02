package io.github.yangxj96.spectra.core.auth.configure.datascope;

import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;

/**
 * 数据范围拦截器
 */
@Slf4j
//@Component
public class DataScopeInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        var scope = DataScopeContext.get();
        // 只处理 SELECT 请求
        // 注解为过滤现线程为true
        if (ms.getSqlCommandType() != SqlCommandType.SELECT || Boolean.FALSE.equals(scope)) {
            return;
        }
        log.atDebug().log("进入拦截:{}", boundSql.getSql());
    }


}
