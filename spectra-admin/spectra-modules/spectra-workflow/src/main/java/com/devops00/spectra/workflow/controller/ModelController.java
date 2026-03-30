package com.devops00.spectra.workflow.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 面向 LogicFlow / 前端设计器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 15:13
@Slf4j
@RestController
@RequestMapping("/workflow/history")
@RequiredArgsConstructor
public class ModelController {

    /*
    职责
    保存流程 JSON（非部署态）
    schema 管理
    草稿
    接口示例
    POST /models
    PUT  /models/{id}
    GET  /models/{id}
    GET  /models
    DELETE /models/{id}
     */

}
