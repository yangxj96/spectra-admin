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

package com.devops00.spectra.core.quartz.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.core.quartz.javabean.from.QuartzHistoryQueryFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobCreateFrom;
import com.devops00.spectra.core.quartz.javabean.from.QuartzJobUpdateFrom;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzExecutionHistoryVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobTypeVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzJobVO;
import com.devops00.spectra.core.quartz.javabean.vo.QuartzTriggerVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;

import java.util.List;
import java.util.UUID;

/** Quartz 管理应用服务；这是应用层唯一允许调用 Quartz 变更 API 的入口。 */
public interface QuartzJobManagementService {

    /** @return 代码白名单中的 Job 类型能力列表；没有定义时返回空列表 */
    List<QuartzJobTypeVO> jobTypes();

    /**
     * @param page Job 分页参数；为空时使用默认分页
     * @return Quartz Job 分页；无匹配时 records 为空且不返回 null
     */
    IPage<QuartzJobVO> jobs(PageFrom page);

    /**
     * 查询单个 Quartz Job。
     *
     * @param jobKey JobKey 名称或包含 group 的完整 JobKey
     * @return Quartz Job 及其唯一 Trigger；目标不存在时抛出数据不存在异常
     */
    QuartzJobVO job(String jobKey);

    /**
     * @param from 普通 Job 创建参数，JobKey 由服务端生成
     * @return 创建完成的 Job；不会返回 null
     */
    QuartzJobVO create(QuartzJobCreateFrom from);

    /**
     * @param jobKey 要修改的 JobKey
     * @param from   修改后的展示信息、参数和唯一 Trigger
     * @return 修改完成的 Job；内置 Job 仍使用代码白名单中的实现类
     */
    QuartzJobVO update(String jobKey, QuartzJobUpdateFrom from);

    /** @param jobKey 要删除的普通 JobKey；内置 Job 不允许删除 */
    void delete(String jobKey);

    /** @param jobKey 要暂停的 JobKey */
    void pause(String jobKey);

    /** @param jobKey 要恢复的 JobKey */
    void resume(String jobKey);

    /** @param jobKey 要立即触发的 JobKey；立即触发不创建新的 Trigger */
    void triggerNow(String jobKey);

    /**
     * 查询 Trigger 详情。
     *
     * @param triggerKey TriggerKey 名称或包含 group 的完整 TriggerKey
     * @return Trigger 安全详情；目标不存在时抛出数据不存在异常
     */
    QuartzTriggerVO triggerDetail(String triggerKey);

    /**
     * 分页查询执行历史。
     *
     * @param page 分页参数；为空时使用默认分页
     * @param from JobKey、TriggerKey、状态和开始时间范围
     * @return 执行历史分页；无匹配时 records 为空且不返回 null
     */
    IPage<QuartzExecutionHistoryVO> executionHistory(PageFrom page, QuartzHistoryQueryFrom from);

    /**
     * 查询单条执行历史详情。
     *
     * @param id 执行历史唯一标识
     * @return 执行历史详情；目标不存在时抛出数据不存在异常
     */
    QuartzExecutionHistoryVO executionHistory(UUID id);
}
