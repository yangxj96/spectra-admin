package com.devops00.spectra.common.mybatis.handler;

import com.devops00.spectra.common.mybatis.base.PgArrayTypeHandler;

public class IntegerArrayTypeHandler extends PgArrayTypeHandler<Integer> {

    public IntegerArrayTypeHandler() {
        super("int4");
    }

}