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

package com.devops00.spectra.framework.configure.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.*;
import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.framework.configure.mybatis.interceptor.DataScopeInnerInterceptor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import java.util.List;


/// MyBatisPlus配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Configuration
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
public class MyBatisPlusConfiguration {

    /// 使用ObjectProvider自动收集所有InnerInterceptor类型的Bean
    @Resource
    private ObjectProvider<InnerInterceptor> innerInterceptors;

    @Resource
    private ObjectProvider<DataScopeProvider> dataScopeProvider;


    /// 添加注释
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        log.debug(LogPrefix.PERSISTENCE.f("载入元数据处理器"));
        return new MetaObjectHandlerImpl();
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.debug(LogPrefix.PERSISTENCE.f("载入MybatisPlusInterceptor"));
        // 分页插件
        var pageInterceptor = new PaginationInnerInterceptor();
        pageInterceptor.setOverflow(true);
        pageInterceptor.setMaxLimit(500L);
        pageInterceptor.setDbType(DbType.POSTGRE_SQL);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(pageInterceptor);
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 数据范围
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(new DataScopeInnerInterceptor(dataScopeProvider)));
        // 收集的bean进行注册
        List<InnerInterceptor> interceptors = innerInterceptors.stream().toList();
        log.debug("{}额外的Interceptor数量{}", LogPrefix.PERSISTENCE.p(), interceptors.size());
        interceptors.forEach(interceptor::addInnerInterceptor);
        return interceptor;
    }

}
