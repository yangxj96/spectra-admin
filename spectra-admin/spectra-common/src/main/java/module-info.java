import org.jspecify.annotations.NullMarked;

@NullMarked
module spectra.common {
    // Dependencies
    requires com.baomidou.mybatis.plus.annotation;
    requires com.baomidou.mybatis.plus.core;
    requires com.baomidou.mybatis.plus.extension;
    requires com.baomidou.mybatis.plus.spring;
    requires com.google.common;
    requires static lombok;
    requires org.apache.tomcat.embed.core;
    requires org.jspecify;
    requires spring.web;

    // Export API packages
    exports io.github.yangxj96.spectra.common.utils;
    exports io.github.yangxj96.spectra.common.response;
    exports io.github.yangxj96.spectra.common.exception;
    exports io.github.yangxj96.spectra.common.constant;

    // Base modules
    exports io.github.yangxj96.spectra.common.base;
    exports io.github.yangxj96.spectra.common.base.javabean.from;
    exports io.github.yangxj96.spectra.common.base.javabean.vo;
}
