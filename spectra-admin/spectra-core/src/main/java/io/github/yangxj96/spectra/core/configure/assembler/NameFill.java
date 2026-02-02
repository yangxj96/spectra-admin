package io.github.yangxj96.spectra.core.configure.assembler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NameFill {

    /// 使用哪个NameLookup,只能是实现类,不能是service的接口
    Class<? extends NameLookup<?>> lookup();

    /// VO 中的 id 字段名
    String sourceField();

}
