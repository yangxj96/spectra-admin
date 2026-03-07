package com.devops00.spectra.common.mybatis.handler;


import com.devops00.spectra.common.mybatis.base.PgArrayTypeHandler;

/// 数据库数组映射到java实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/7 23:52
public class StringArrayTypeHandler extends PgArrayTypeHandler<String> {

    public StringArrayTypeHandler() {
        super("text");
    }

}
