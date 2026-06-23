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

package com.devops00.spectra.oa.report.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.report.javabean.entity.Report;
import com.devops00.spectra.oa.report.mapper.ReportMapper;
import com.devops00.spectra.oa.report.service.ReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 报表表-服务默认实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/30 11:44
 */
@Slf4j
@Service
public class ReportServiceImpl extends BaseServiceImpl<ReportMapper, Report> implements ReportService {
}
