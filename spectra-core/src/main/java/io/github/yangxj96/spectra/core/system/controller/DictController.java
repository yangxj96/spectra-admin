/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.system.controller;

import cn.dev33.satoken.annotation.SaCheckEL;
import cn.dev33.satoken.annotation.SaCheckLogin;
import io.github.yangxj96.spectra.common.annotation.ULog;
import io.github.yangxj96.spectra.common.base.Verify;
import io.github.yangxj96.spectra.core.system.javabean.from.DictDataFrom;
import io.github.yangxj96.spectra.core.system.javabean.from.DictGroupFrom;
import io.github.yangxj96.spectra.core.system.javabean.vo.DictDataVo;
import io.github.yangxj96.spectra.core.system.javabean.vo.DictTypeTreeVO;
import io.github.yangxj96.spectra.core.system.service.DictService;
import jakarta.annotation.Resource;
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
     * 创建字典组
     *
     * @param params 请求参数
     */
    @ULog("创建字典组")
    @PostMapping("/group")
    @SaCheckEL("@ss.hasPermission('DICT:INSERT')")
    public void createGroup(@Validated(Verify.Insert.class) @RequestBody DictGroupFrom params) {
        bindService.createGroup(params);
    }

    /**
     * 删除字典组
     *
     * @param id 字典组ID
     */
    @ULog("删除字典组")
    @DeleteMapping("/group/{id}")
    @SaCheckEL("@ss.hasPermission('DICT:DELETE')")
    public void deleteGroup(@PathVariable String id) {
        bindService.deleteGroup(Long.parseLong(id));
    }

    /**
     * 修改字典组
     *
     * @param params 请求参数
     */
    @ULog("修改字典组")
    @PutMapping("/group")
    @SaCheckEL("@ss.hasPermission('DICT:UPDATE')")
    public void modifyGroup(@Validated(Verify.Update.class) @RequestBody DictGroupFrom params) {
        bindService.modifyGroup(params);
    }

    /**
     * 创建字典项
     *
     * @param params 请求参数
     */
    @ULog("创建字典数据")
    @PostMapping("/data")
    @SaCheckEL("@ss.hasPermission('DICT:INSERT')")
    public void createData(@Validated(Verify.Insert.class) @RequestBody DictDataFrom params) {
        bindService.createData(params);
    }

    /**
     * 删除字典项
     *
     * @param id 字典项ID
     */
    @ULog("删除字典项")
    @DeleteMapping("/data/{id}")
    @SaCheckEL("@ss.hasPermission('DICT:DELETE')")
    public void deleteData(@PathVariable String id) {
        bindService.deleteData(Long.parseLong(id));
    }

    /**
     * 修改字典项
     *
     * @param params 请求参数
     */
    @ULog("修改字典数据")
    @PutMapping("/data")
    @SaCheckEL("@ss.hasPermission('DICT:UPDATE')")
    public void modifyData(@Validated(Verify.Update.class) @RequestBody DictDataFrom params) {
        bindService.modifyData(params);
    }

    /**
     * 获取所有字典组的树形列表
     *
     * @return 字典组树
     */
    @ULog("获取所有字典类型的树形列表")
    @GetMapping("/group/tree")
    public List<DictTypeTreeVO> listDictGroupWrapTree() {
        return bindService.listDictGroupWrapTree();
    }

    /**
     * 根据类型编码获取字典项
     *
     * @param code 对应数据类型的code
     * @return 字典项列表
     */
    @ULog("根据类型编码获取字典数据")
    @GetMapping("/data/{code}")
    public List<DictDataVo> listDictDataByGroupCode(@PathVariable String code) {
        return bindService.listDictDataByGroupCode(code);
    }
}
