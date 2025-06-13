package com.yangxj96.spectra.core.annotation;

import java.lang.annotation.*;

/**
 * 操作日志记录注解
 *
 * @author Jack Young
 * @since 2025年6月5日 10点44分
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ULog {

    /**
     * 操作说明
     *
     * @return 操作说明
     */
    String value() default "未填写操作说明";

}
