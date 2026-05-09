package com.devops00.spectra.core.configure.mybatis.interceptor;


import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.devops00.spectra.common.constant.LogPrefix;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.util.Map;

/**
 * 注入通用数据范围参数
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/5/9 17:35
 */
@Slf4j
@Component
public class DataScopeInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {
        BoundSql boundSql = sh.getBoundSql();
        Object parameterObject = boundSql.getParameterObject();
        if (parameterObject instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) parameterObject;
            log.debug("{}获取传入的参数:{}", LogPrefix.PERSISTENCE.p(), map.getOrDefault("_ctx_sc", "").toString());
        }
    }

}
