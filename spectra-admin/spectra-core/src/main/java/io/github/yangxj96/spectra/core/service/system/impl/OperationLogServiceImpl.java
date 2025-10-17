/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.service.system.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.yangxj96.spectra.core.javabean.system.entity.OperationLog;
import io.github.yangxj96.spectra.core.mapper.system.OperationLogMapper;
import io.github.yangxj96.spectra.core.service.system.OperationLogService;
import org.springframework.stereotype.Service;

/**
 * 操作日志service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {
}
