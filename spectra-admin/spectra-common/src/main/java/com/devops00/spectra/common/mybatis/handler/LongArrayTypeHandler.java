package com.devops00.spectra.common.mybatis.handler;

import com.devops00.spectra.common.mybatis.base.PgArrayTypeHandler;

public class LongArrayTypeHandler extends PgArrayTypeHandler<Long> {

    public LongArrayTypeHandler() {
        super("int8");
    }

}