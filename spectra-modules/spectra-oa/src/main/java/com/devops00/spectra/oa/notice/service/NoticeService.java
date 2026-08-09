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

import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.notice.javabean.entity.Notice;
import com.devops00.spectra.oa.notice.javabean.from.NoticeCreateFrom;
import com.devops00.spectra.oa.notice.javabean.from.NoticePageFrom;
import com.devops00.spectra.oa.notice.javabean.vo.NoticeVO;

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
     */
    IPage<NoticeVO> page(PageFrom page, NoticePageFrom params);

    /**
     * 查询公告详情。
     */
    NoticeVO get(UUID id);

    /**
     * 创建公告草稿。
     */
    Notice createDraft(NoticeCreateFrom from);

    /**
     * 发布公告。
     */
    void publish(UUID id);

    /**
     * 撤回公告。
     */
    void revoke(UUID id);

    /**
     * 标记公告已读。
     */
    void markRead(UUID id);
}
