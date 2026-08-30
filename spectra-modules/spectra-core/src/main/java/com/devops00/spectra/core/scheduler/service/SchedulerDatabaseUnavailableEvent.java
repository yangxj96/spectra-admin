/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.scheduler.service;

import org.springframework.dao.DataAccessException;

/**
 * 异步离散执行发现数据库故障时通知调度启动门禁。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
public record SchedulerDatabaseUnavailableEvent(DataAccessException cause) {
}
