import org.jspecify.annotations.NullMarked;

@NullMarked
module spectra.core {
    // --- Spring Core ---
    requires spring.beans;
    requires spring.context;
    requires spring.core;
    requires spring.tx;

    // --- Spring Web ---
    requires spring.web;
    requires spring.webmvc;

    // --- Spring Boot ---
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.boot.jackson;

    // --- Spring Data ---
    requires spring.data.commons;
    requires spring.data.redis;
    requires spring.jdbc;

    // --- Spring Security ---
    requires spring.security.core;
    requires spring.security.crypto;
    requires spring.security.config;
    requires spring.security.web;

    // --- MyBatis / MyBatis-Plus ---
    requires org.mybatis;
    requires org.mybatis.spring;
    requires com.baomidou.mybatis.plus.core;
    requires com.baomidou.mybatis.plus.extension;
    requires com.baomidou.mybatis.plus.annotation;
    requires com.baomidou.mybatis.plus.spring;
    requires com.baomidou.mybatis.plus.jsqlparser;

    // --- Jackson ---
    requires com.fasterxml.jackson.annotation;
    requires tools.jackson.databind;

    // --- Other libs ---
    requires org.apache.tomcat.embed.core;
    requires org.apache.commons.lang3;
    requires org.apache.tika.core;
    requires com.github.oshi;
    requires jakarta.annotation;
    requires jakarta.validation;
    requires java.desktop;
    requires java.management;
    requires java.sql;

    requires kaptcha;
    requires ip2region;
    requires org.aspectj.weaver;
    requires org.mapstruct;
    requires org.jspecify;

    // Compile-time only
    requires static lombok;
    requires spectra.common;

    // Reflection opens
    opens io.github.yangxj96.spectra.core to spring.core, spring.beans, spring.context, spring.web, tools.jackson.databind;

}