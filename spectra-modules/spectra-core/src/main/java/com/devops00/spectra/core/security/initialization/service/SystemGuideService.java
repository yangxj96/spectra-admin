/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.security.initialization.service;

import com.devops00.spectra.core.security.initialization.javabean.from.SystemGuideCompleteFrom;
import com.devops00.spectra.core.security.initialization.javabean.vo.SystemGuideStatusVO;

/**
 * DEV_OPS 首次进入系统时的设置引导。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SystemGuideService {

    /**
     * 查询当前用户的引导状态。
     *
     * @return 返回当前用户的首次进入引导状态及尚未完成的设置项；系统状态读取失败时抛出业务异常，不返回 null。
     */
    SystemGuideStatusVO status();

    /**
     * 保存引导设置并完成引导。
     *
     * @param from 引导设置
     */
    void complete(SystemGuideCompleteFrom from);
}
