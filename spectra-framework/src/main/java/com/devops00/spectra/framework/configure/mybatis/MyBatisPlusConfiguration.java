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
import com.devops00.spectra.framework.configure.mybatis.interceptor.DataScopeInnerInterceptor;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.RollbackOn;

import java.util.List;

/**
 * MyBatisPlus配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Slf4j
@Configuration
@EnableTransactionManagement(rollbackOn = RollbackOn.ALL_EXCEPTIONS)
public class MyBatisPlusConfiguration {

    /**
     * 使用ObjectProvider自动收集所有InnerInterceptor类型的Bean
     */
    @Resource
    private ObjectProvider<InnerInterceptor> innerInterceptors;

    @Resource
    private ObjectProvider<AuthorizationSnapshotProvider> authorizationSnapshotProvider;

    @Resource
    private DataScopeEntityRegistry dataScopeEntityRegistry;

    /**
     * 添加审计字段自动填充处理器。
     *
     * <p>安全上下文访问器的实现会读取安全会话，而安全会话的用户加载器又会使用
     * MyBatis。这里延迟注入只把访问器的解析推迟到实际填充审计字段时，避免
     * MyBatis 基础设施初始化阶段形成反向依赖；不会改变运行期的当前用户解析行为。</p>
     */
    @Bean
    public MetaObjectHandler metaObjectHandler(@Lazy SecurityContextAccessor securityContextAccessor) {
        log.debug(LogPrefix.PERSISTENCE.f("载入元数据处理器"));
        return new MetaObjectHandlerImpl(securityContextAccessor);
    }

    /**
     * 创建 MyBatis-Plus 拦截器链。
     *
     * <p>数据权限拦截器只在 SQL 执行时读取当前用户。使用延迟代理可以让数据库与
     * 安全会话基础设施先完成初始化，避免出现
     * {@code SecurityContextAccessor -> sec -> SecurityUserLoader -> MyBatis} 的启动环。</p>
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(@Lazy SecurityContextAccessor securityContextAccessor) {
        log.debug(LogPrefix.PERSISTENCE.f("载入MybatisPlusInterceptor"));
        // 数据权限必须先于分页插件处理原始查询。PaginationInnerInterceptor
        // 会在 willDoQuery 阶段生成 count SQL；若顺序反过来，count 查询会
        // 先执行而绕过数据权限谓词，导致分页总数发生越权。
        var dataPermissionInterceptor = new DataPermissionInterceptor(new DataScopeInnerInterceptor(
                authorizationSnapshotProvider, dataScopeEntityRegistry, securityContextAccessor));
        // 分页插件
        var pageInterceptor = new PaginationInnerInterceptor();
        pageInterceptor.setOverflow(true);
        pageInterceptor.setMaxLimit(500L);
        pageInterceptor.setDbType(DbType.POSTGRE_SQL);
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(dataPermissionInterceptor);
        interceptor.addInnerInterceptor(pageInterceptor);
        // 针对 update 和 delete 语句 作用: 阻止恶意的全表更新删除
        interceptor.addInnerInterceptor(new BlockAttackInnerInterceptor());
        // 乐观锁
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        // 收集的bean进行注册
        List<InnerInterceptor> interceptors = innerInterceptors.stream().toList();
        log.debug("{}额外的Interceptor数量{}", LogPrefix.PERSISTENCE.p(), interceptors.size());
        interceptors.forEach(interceptor::addInnerInterceptor);
        return interceptor;
    }
}
