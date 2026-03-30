package com.devops00.spectra.workflow.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 面向“查询 + 审计”
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 15:12
@Slf4j
@RestController
@RequestMapping("/workflow/history")
@RequiredArgsConstructor
public class HistoryController {

    /*
    👉 面向“查询 + 审计”
    职责
    历史任务
    审批记录
    流程轨迹
    接口示例
    GET /history/process-instances/{id}
    GET /history/tasks
    GET /history/activities
     */

}
