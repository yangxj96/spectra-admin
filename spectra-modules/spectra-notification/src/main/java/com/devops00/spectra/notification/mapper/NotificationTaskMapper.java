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

package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * 通知任务 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Mapper
public interface NotificationTaskMapper extends BaseMapper<NotificationTaskEntity> {

    /**
     * 按计划时间领取待处理任务，并使用 PostgreSQL 行锁跳过已被其他 Worker 锁定的任务。
     */
    @InterceptorIgnore(dataPermission = "true")
    List<NotificationTaskEntity> selectPendingTasks(@Param("now") Instant now, @Param("limit") int limit);
}
