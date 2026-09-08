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

package com.devops00.spectra.oa.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.notice.javabean.entity.Notice;
import com.devops00.spectra.oa.notice.javabean.from.NoticeCreateFrom;
import com.devops00.spectra.oa.notice.javabean.from.NoticePageFrom;
import com.devops00.spectra.oa.notice.javabean.vo.NoticeVO;

import java.util.UUID;

/**
 * 公告业务服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
public interface NoticeService extends BaseService<Notice> {
    /**
     * 分页查询公告。
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 公告关键字和发布状态等公告分页筛选条件。
     * @return 返回按分页条件查询的OA 公告分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<NoticeVO> page(PageFrom page, NoticePageFrom params);

    /**
     * 查询公告详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 公告详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    NoticeVO get(UUID id);

    /**
     * 创建公告草稿。
     *
     * @param from 公告标题、摘要、正文、发布对象、目标部门、必读标记和发布时间等草稿字段。
     * @return 返回已创建的公告草稿实体，包含标题、内容和草稿状态；校验或写入失败时抛出业务异常，不返回 null。
     */
    Notice createDraft(NoticeCreateFrom from);

    /**
     * 发布公告。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void publish(UUID id);

    /**
     * 撤回公告。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void revoke(UUID id);

    /**
     * 标记公告已读。
     *
     * @param id 目标OA 业务记录的唯一标识。
     */
    void markRead(UUID id);
}
