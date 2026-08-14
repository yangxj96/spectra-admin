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

package com.devops00.spectra.oa.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.notice.javabean.from.NoticeCreateFrom;
import com.devops00.spectra.oa.notice.javabean.from.NoticePageFrom;
import com.devops00.spectra.oa.notice.javabean.vo.NoticeVO;
import com.devops00.spectra.oa.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 公告中心接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@RestController
@RequestMapping("/oa/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 查询公告列表。
     */
    @ULog("'查询公告列表'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:notice:read')")
    public IPage<NoticeVO> page(PageFrom page, NoticePageFrom params) {
        return noticeService.page(page, params);
    }

    /**
     * 获取公告详情。
     */
    @ULog("'获取公告详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:notice:read')")
    public NoticeVO get(@PathVariable UUID id) {
        return noticeService.get(id);
    }

    /**
     * 创建公告草稿。
     */
    @ULog("'创建公告草稿'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:notice:create')")
    public NoticeVO create(@Validated(Verify.Insert.class) @RequestBody NoticeCreateFrom from) {
        return noticeService.get(noticeService.createDraft(from).getId());
    }

    /**
     * 发布公告。
     */
    @ULog("'发布公告'")
    @PostMapping(value = "/{id}/publish", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:notice:update')")
    public void publish(@PathVariable UUID id) {
        noticeService.publish(id);
    }

    /**
     * 撤回公告。
     */
    @ULog("'撤回公告'")
    @PostMapping(value = "/{id}/revoke", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:notice:update')")
    public void revoke(@PathVariable UUID id) {
        noticeService.revoke(id);
    }

    /**
     * 标记公告已读。
     */
    @ULog("'标记公告已读'")
    @PutMapping(value = "/{id}/read", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'oa:notice:update')")
    public void markRead(@PathVariable UUID id) {
        noticeService.markRead(id);
    }
}
