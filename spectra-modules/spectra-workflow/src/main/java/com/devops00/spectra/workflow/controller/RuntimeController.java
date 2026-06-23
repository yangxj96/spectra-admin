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

package com.devops00.spectra.workflow.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 面向“流程控制能力”
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/30 15:11
@Slf4j
@RestController
@RequestMapping("/workflow/runtime")
@RequiredArgsConstructor
public class RuntimeController {

    /*
    👉 面向“流程控制能力”
    职责
    跳转节点（很重要）
    回退
    加签 / 减签
    动态修改变量
    接口示例
    POST /runtime/tasks/{id}/jump
    POST /runtime/tasks/{id}/rollback
    POST /runtime/tasks/{id}/add-sign
    POST /runtime/tasks/{id}/remove-sign

    POST /runtime/variables
     */

}
