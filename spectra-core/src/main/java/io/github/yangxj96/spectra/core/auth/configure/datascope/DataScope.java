package io.github.yangxj96.spectra.core.auth.configure.datascope;

import java.lang.annotation.*;

/**
 * 数据范围注解
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    String value() default "all";

    /**
     * 是否进行过滤
     * <p>默认需要过滤,预留这个接口主要是为了临时方法不过滤使用</p>
     * @return 是否进行过滤
     */
    boolean filter() default true;
}
