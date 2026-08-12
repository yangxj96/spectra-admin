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

import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.core.system.javabean.from.DictGroupFrom;
import com.devops00.spectra.core.system.javabean.from.DictItemFrom;
import com.devops00.spectra.core.system.javabean.vo.DictGroupTreeVO;
import com.devops00.spectra.core.system.javabean.vo.DictItemVO;
import com.devops00.spectra.core.system.service.DictService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * 字典控制器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/18 00:00
 */
@Slf4j
@RestController
@RequestMapping("/dict")
public class DictController {

    private final DictService bindService;

    public DictController(DictService bindService) {
        this.bindService = bindService;
    }

    /**
     * 创建字典组
     *
     * @param params 请求参数
     */
    @ULog("'创建字典组'")
    @PostMapping(value = "/group", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:INSERT')")
    public void createGroup(@Validated(Verify.Insert.class) @RequestBody DictGroupFrom params) {
        bindService.createGroup(params);
    }

    /**
     * 删除字典组
     *
     * @param id 字典组ID
     */
    @ULog("'删除字典组'")
    @DeleteMapping(value = "/group/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:DELETE')")
    public void deleteGroup(@PathVariable UUID id) {
        bindService.deleteGroup(id);
    }

    /**
     * 修改字典组
     *
     * @param params 请求参数
     */
    @ULog("'修改字典组'")
    @PutMapping(value = "/group", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:UPDATE')")
    public void modifyGroup(@Validated(Verify.Update.class) @RequestBody DictGroupFrom params) {
        bindService.modifyGroup(params);
    }

    /**
     * 创建字典项
     *
     * @param params 请求参数
     */
    @ULog("'创建字典数据'")
    @PostMapping(value = "/data", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:INSERT')")
    public void createData(@Validated(Verify.Insert.class) @RequestBody DictItemFrom params) {
        bindService.createData(params);
    }

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     */
    @ULog("'删除字典项'")
    @DeleteMapping(value = "/data/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:DELETE')")
    public void deleteData(@PathVariable UUID id) {
        bindService.deleteData(id);
    }

    /**
     * 修改字典项
     *
     * @param params 请求参数
     */
    @ULog("'修改字典数据'")
    @PutMapping(value = "/data", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:UPDATE')")
    public void modifyData(@Validated(Verify.Update.class) @RequestBody DictItemFrom params) {
        bindService.modifyData(params);
    }

    /**
     * 获取所有字典组的树形列表
     *
     * @return 字典组树
     */
    @ULog("'获取所有字典类型的树形列表'")
    @GetMapping(value = "/group/tree", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:QUERY')")
    public List<DictGroupTreeVO> listDictGroupWrapTree() {
        return bindService.listDictGroupWrapTree();
    }

    /**
     * 根据类型编码获取字典项
     *
     * @param code 对应数据类型的code
     * @return 字典项列表
     */
    @ULog("'根据类型编码获取字典数据'")
    @GetMapping(value = "/data/{code}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'DICT:QUERY')")
    public List<DictItemVO> listDictDataByGroupCode(@PathVariable String code) {
        return bindService.listDictDataByGroupCode(code);
    }
}
