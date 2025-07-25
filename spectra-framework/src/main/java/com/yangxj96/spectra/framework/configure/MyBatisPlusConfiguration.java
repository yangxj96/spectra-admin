/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.framework.configure;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.BlockAttackInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.yangxj96.spectra.framework.mybatis.MetaObjectHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

/**
 * MyBatisPlus配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Configuration
@MapperScan("com.yangxj96.spectra.**.mapper")
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
public class MyBatisPlusConfiguration {

    private static final String PREFIX = "[MyBatisPlus]:";

    @Bean
    @ConditionalOnClass(StpUtil.class)
    public MetaObjectHandler metaObjectHandler() {
        log.atDebug().log(PREFIX + "载入元数据处理器");
        return new MetaObjectHandlerImpl();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.atDebug().log(PREFIX + "载入MybatisPlusInterceptor");
        // 分页插件
        var pageInterceptor = new PaginationInnerInterceptor();
        pageInterceptor.setOverflow(true);
        pageInterceptor.setMaxLimit(500L);
        pageInterceptor.setDbType(DbType.MYSQL);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(pageInterceptor);
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        return interceptor;
    }

}
