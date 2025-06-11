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


import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;

import java.time.LocalDateTime;

/**
 * 元数据填充实现
 *
 * @author 杨新杰
 * @since 2025/6/5 11:02
 */
@Slf4j
public class MetaObjectHandlerImpl implements MetaObjectHandler {

    /**
     * 创建人
     */
    private static final String CREATED_BY = "createdBy";

    /**
     * 创建时间
     */
    private static final String CREATED_AT = "createdAt";

    /**
     * 更新人
     */
    private static final String UPDATED_BY = "updatedBy";

    /**
     * 更新时间
     */
    private static final String UPDATED_AT = "updatedAt";


    @Override
    public void insertFill(MetaObject metaObject) {
        log.atDebug().log("[MyBatisPlus] 新增填充字段");
        if (getFieldValByName(CREATED_BY, metaObject) == null) {
            setFieldValByName(CREATED_BY, getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(CREATED_AT, metaObject) == null) {
            setFieldValByName(CREATED_AT, LocalDateTime.now(), metaObject);
        }
        if (getFieldValByName(UPDATED_BY, metaObject) == null) {
            setFieldValByName(UPDATED_BY, getCurrentUserId(), metaObject);
        }
        if (getFieldValByName(UPDATED_AT, metaObject) == null) {
            setFieldValByName(UPDATED_AT, LocalDateTime.now(), metaObject);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.atDebug().log("[MyBatisPlus] 修改填充字段");
        setFieldValByName(UPDATED_BY, getCurrentUserId(), metaObject);
        setFieldValByName(UPDATED_AT, LocalDateTime.now(), metaObject);
    }

    /**
     * 获取当前用户ID,如果获取失败,则返回ID为0
     *
     * @return 用户ID
     */
    private Long getCurrentUserId() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.atError().log("获取ID出错,默认ID为0");
            return 0L;
        }
    }
}
