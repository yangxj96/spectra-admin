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
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.system.javabean.from.RegionFrom;
import com.devops00.spectra.core.system.javabean.from.RegionPageFrom;
import com.devops00.spectra.core.system.javabean.vo.RegionPathVO;
import com.devops00.spectra.core.system.javabean.vo.RegionVO;
import com.devops00.spectra.core.system.service.RegionService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/// 行政区划相关接口
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/1/30 15:32
@Slf4j
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
    @ULog("'获取行政区划懒加载树'")
    @GetMapping(value = "/lazy", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public List<RegionVO> lazyTree(Integer level, @RequestParam(value = "id", required = false) String id) {
        return bindService.lazyTree(level, id);
    }

    @ULog("'分页查询行政区划'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public IPage<RegionVO> page(PageFrom page, RegionPageFrom params) {
        return bindService.page(page, params);
    }

    @ULog("'获取行政区划路径'")
    @GetMapping(value = "/path/{id}", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public RegionPathVO getPath(@PathVariable UUID id) {
        return bindService.getPath(id);
    }

    /// 新增行政区划
    ///
    /// @param params 行政区划信息
    /// @return 新增后的行政区划信息
    @ULog("'新增行政区划'")
    @PostMapping(value = "/created", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public RegionVO created(@Validated(Verify.Insert.class) @RequestBody RegionFrom params) {
        return bindService.created(params);
    }

    /// 修改行政区划
    ///
    /// @param params 行政区划信息
    /// @return 修改后的行政区划信息
    @ULog("'修改行政区划'")
    @PutMapping(value = "/modify", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public RegionVO modify(@Validated(Verify.Update.class) @RequestBody RegionFrom params) {
        return bindService.modify(params);
    }

    /// 根据ID删除行政区划
    ///
    /// @param id 行政区划ID
    @ULog("'删除行政区划'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public void deleteById(@PathVariable UUID id) {
        bindService.deleteById(id);
    }

}
