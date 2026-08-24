/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.service;

import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationMfaConfirmFrom;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationStartFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationMfaConfirmVO;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStartVO;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;

/** 系统首次初始化流程。 */
public interface SystemInitializationService {

    /**
     * 查询或获取目标数据（{@code status}）。
     */
    SystemInitializationStatusVO status();

    /**
     * 创建或构建目标数据（{@code start}）。
     */
    SystemInitializationStartVO start(SystemInitializationStartFrom from, String initializationToken);

    /**
     * 处理内部业务逻辑（{@code confirmMfa}）。
     */
    SystemInitializationMfaConfirmVO confirmMfa(SystemInitializationMfaConfirmFrom from);

    /**
     * 处理内部业务逻辑（{@code complete}）。
     */
    void complete(SystemInitializationCompleteFrom from);
}
