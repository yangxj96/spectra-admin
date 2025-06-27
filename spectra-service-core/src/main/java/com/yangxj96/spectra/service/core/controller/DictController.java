/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.core.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.yangxj96.spectra.common.base.Verify;
import com.yangxj96.spectra.service.core.javabean.from.DictDataFrom;
import com.yangxj96.spectra.service.core.javabean.from.DictTypeFrom;
import com.yangxj96.spectra.service.core.javabean.vo.DictDataVo;
import com.yangxj96.spectra.service.core.javabean.vo.DictTypeTreeVO;
import com.yangxj96.spectra.service.core.service.DictService;
import com.yangxj96.spectra.starter.common.annotation.ULog;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 字典控制器
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/18
 */
@SaCheckLogin
@RestController
@RequestMapping("/dict")
public class DictController {

    @Resource
    private DictService bindService;

    /**
     * 获取所有字典类型的树形列表
     *
     * @return 字典类型树
     */
    @ULog("获取所有字典类型的树形列表")
    @GetMapping("/type/tree")
    public List<DictTypeTreeVO> getTypesGroupTree() {
        return bindService.getTypesWrapTree();
    }

    /**
     * 根据类型编码获取字典数据
     *
     * @param code 对应数据类型的code
     * @return 字典数据列表
     */
    @ULog("根据类型编码获取字典数据")
    @GetMapping("/data/{code}")
    public List<DictDataVo> getDataByTypeCode(@PathVariable String code) {
        return bindService.getDataByTypeCode(code);
    }

    /**
     * 创建字典类型
     *
     * @param params 请求参数
     */
    @ULog("创建字典类型")
    @PostMapping("/createType")
    @ResponseStatus(HttpStatus.CREATED)
    @SaCheckPermission(value = "DICT:INSERT", orRole = "DEV_SYSADMIN")
    public void createType(@Validated(Verify.Insert.class) @RequestBody DictTypeFrom params) {
        bindService.createType(params);
    }

    /**
     * 创建字典数据
     *
     * @param params 请求参数
     */
    @ULog("创建字典数据")
    @PostMapping("/createData")
    @ResponseStatus(HttpStatus.CREATED)
    @SaCheckPermission(value = "DICT:INSERT", orRole = "DEV_SYSADMIN")
    public void createData(@Validated(Verify.Insert.class) @RequestBody DictDataFrom params) {
        bindService.createData(params);
    }


}
