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

/** DEV_OPS 首次进入系统时的设置引导。 */
public interface SystemGuideService {

    /**
     * 查询当前用户的引导状态。
     *
     * @return 引导状态
     */
    SystemGuideStatusVO status();

    /**
     * 保存引导设置并完成引导。
     *
     * @param from 引导设置
     */
    void complete(SystemGuideCompleteFrom from);
}
