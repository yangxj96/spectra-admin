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

package com.devops00.spectra.core.system.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.system.javabean.from.RegionPageFrom;
import com.devops00.spectra.core.system.javabean.vo.RegionPathVO;
import com.devops00.spectra.core.system.javabean.vo.RegionVO;
import com.devops00.spectra.core.system.service.RegionService;
import com.devops00.spectra.log.base.annotation.ULog;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 行政区划相关接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/1/30 15:32
@RestController
@RequestMapping("/region")
public class RegionController {

    private final RegionService bindService;

    public RegionController(RegionService regionService) {
        this.bindService = regionService;
    }

    /// 懒加载树
    ///
    /// @param level 层级
    /// @param id    父级ID
    /// @return 根据条件获取的下级的列表
    @GetMapping("/lazy", version = "1.0.0+")
    public List<RegionVO> lazyTree(Integer level, @RequestParam(value = "id", required = false) String id) {
        return bindService.lazyTree(level, id);
    }

    @ULog("'分页查询行政区划'")
    @GetMapping("/page", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public IPage<RegionVO> page(PageFrom page, RegionPageFrom params) {
        return bindService.page(page, params);
    }

    @GetMapping("/path/{id}", version = "1.0.0+")
    public RegionPathVO getPath(@PathVariable UUID id) {
        return bindService.getPath(id);
    }

}
