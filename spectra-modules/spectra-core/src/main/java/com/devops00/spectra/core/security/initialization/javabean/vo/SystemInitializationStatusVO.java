/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.initialization.javabean.vo;

/** 系统初始化状态。 */
public record SystemInitializationStatusVO(String state, boolean initialized, boolean initializationRequired) {
}
