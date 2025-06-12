package com.yangxj96.spectra.core.configuration.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatisPlus配置
 *
 * @author 杨新杰
 * @since 2025/6/5 10:58
 */
@Slf4j
@Configuration
public class MyBatisPlusConfiguration {

    /** 最大查询分页数量 **/
    private static final Long MAX_LIMIT = 500L;

    @Bean
    MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandlerImpl();
    }

    @Bean
    MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.debug("载入MybatisPlusInterceptor");
        // 分页插件
        var pageInterceptor = new PaginationInnerInterceptor();
        pageInterceptor.setOverflow(true);
        pageInterceptor.setMaxLimit(MAX_LIMIT);
        pageInterceptor.setDbType(DbType.POSTGRE_SQL);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(pageInterceptor);
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
