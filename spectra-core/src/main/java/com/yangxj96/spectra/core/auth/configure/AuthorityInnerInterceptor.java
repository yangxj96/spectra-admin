package com.yangxj96.spectra.core.auth.configure;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.stereotype.Component;

/**
 * 权限拦截插件
 */
@Slf4j
@Component
public class AuthorityInnerInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor,
                            MappedStatement ms,
                            Object parameter,
                            RowBounds rowBounds,
                            ResultHandler resultHandler,
                            BoundSql boundSql) {
        log.atDebug().log("MybatisPlus权限拦截器,用户是否登录:{}", StpUtil.isLogin());
        if (StpUtil.isLogin()) {
            log.atDebug().log("用户TOKEN:{}", StpUtil.getTokenValue());
        }
    }
}
