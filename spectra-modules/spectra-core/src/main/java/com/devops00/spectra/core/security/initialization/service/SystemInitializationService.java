/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.service;

import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.from.SystemInitializationStartFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStartVO;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;

/** 系统首次初始化流程。 */
public interface SystemInitializationService {

    /**
     * 查询或获取目标数据（{@code status}）。
     *
     * @return 返回系统初始化状态；处理失败时抛出业务异常，不返回 null。
     */
    SystemInitializationStatusVO status();

    /**
     * 创建或构建目标数据（{@code start}）。
     *
     * @param from                初始管理员用户名、密码、姓名及系统名称、语言、时区和安全档案配置。
     * @param initializationToken 首次初始化使用的一次性令牌；用于证明调用方已通过初始化前置校验，使用后不得复用。
     * @return 返回系统初始化启动结果；处理失败时抛出业务异常，不返回 null。
     */
    SystemInitializationStartVO start(SystemInitializationStartFrom from, String initializationToken);

    /**
     * 处理内部业务逻辑（{@code complete}）。
     *
     * @param from 要完成的初始化流程标识，用于确认启动结果并推进系统初始化状态。
     */
    void complete(SystemInitializationCompleteFrom from);
}
