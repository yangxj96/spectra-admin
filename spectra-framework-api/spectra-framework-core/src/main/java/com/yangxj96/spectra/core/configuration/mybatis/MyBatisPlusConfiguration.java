/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
