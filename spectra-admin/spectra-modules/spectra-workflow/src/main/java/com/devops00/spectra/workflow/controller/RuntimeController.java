package com.devops00.spectra.workflow.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 面向“流程控制能力”
///
/// @author Jack Young
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
