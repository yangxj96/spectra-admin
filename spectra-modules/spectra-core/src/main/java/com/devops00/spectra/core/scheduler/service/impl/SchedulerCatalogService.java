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

package com.devops00.spectra.core.scheduler.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobPageFrom;
import com.devops00.spectra.core.scheduler.javabean.from.SchedulerJobSaveFrom;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerCatalogVO;
import com.devops00.spectra.core.scheduler.javabean.vo.SchedulerJobVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** 调度任务目录的查询、注册和定义修改用例。 */
@Service
@RequiredArgsConstructor
public class SchedulerCatalogService {

    private final SchedulerAdminServiceSupport support;

    public List<SchedulerCatalogVO> catalog() {
        return support.catalog();
    }

    public IPage<SchedulerJobVO> jobs(PageFrom page, SchedulerJobPageFrom from) {
        return support.jobs(page, from);
    }

    @Transactional
    public SchedulerJobVO create(SchedulerJobSaveFrom from) {
        return support.create(from);
    }

    @Transactional
    public SchedulerJobVO update(UUID id, SchedulerJobSaveFrom from) {
        return support.update(id, from);
    }
}
