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

package com.devops00.spectra.core.notification.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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
     * 按通知请求和候选幂等键批量查询已存在的任务。
     *
     * @param requestId 通知请求 ID
     * @param tasks     候选任务，仅使用其中的接收人哈希和渠道
     * @return 已存在的任务键
     */
    List<NotificationTaskEntity> selectExistingTasks(@Param("requestId") UUID requestId,
                                                     @Param("tasks") List<NotificationTaskEntity> tasks);

    /**
     * 批量插入通知任务。
     *
     * @param tasks 待插入任务
     * @return 实际插入行数
     */
    int insertBatch(@Param("tasks") List<NotificationTaskEntity> tasks);

    /**
     * 按计划时间领取待处理任务，并使用 PostgreSQL 行锁跳过已被其他 Worker 锁定的任务。
     */
    @InterceptorIgnore(dataPermission = "true")
    List<NotificationTaskEntity> selectPendingTasks(@Param("now") Instant now, @Param("limit") int limit);

    /**
     * 批量清理已过期或已超过保留窗口的敏感任务载荷。
     */
    @InterceptorIgnore(dataPermission = "true")
    int clearSensitivePayloads(@Param("now") Instant now, @Param("cutoff") Instant cutoff,
                               @Param("limit") int limit);
}
