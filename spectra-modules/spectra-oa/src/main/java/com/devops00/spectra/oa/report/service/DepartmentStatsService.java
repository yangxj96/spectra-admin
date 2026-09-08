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

package com.devops00.spectra.oa.report.service;

import com.devops00.spectra.oa.report.javabean.from.DepartmentStatsFrom;
import com.devops00.spectra.oa.report.javabean.vo.DepartmentStatsVO;

import java.util.List;

/**
 * 部门维度统计服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
public interface DepartmentStatsService {

    /**
     * 查询部门维度统计。
     *
     * @param from 查询条件
     * @return 返回按部门汇总的人员或业务统计列表；没有符合条件的部门时返回空列表，不返回 null。
     */
    List<DepartmentStatsVO> list(DepartmentStatsFrom from);

    /**
     * 导出部门维度统计 Excel。
     *
     * @param from 查询条件
     * @return 返回按统计条件生成的 XLSX 文件字节；没有可导出的数据时仍返回合法的空报表文件，生成失败时抛出异常，不返回 null 或空数组。
     */
    byte[] export(DepartmentStatsFrom from);
}
