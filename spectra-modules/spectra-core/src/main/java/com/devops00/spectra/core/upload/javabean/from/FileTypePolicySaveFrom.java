/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import tools.jackson.databind.JsonNode;

/** 文件类型策略保存请求。 */
@Data
public class FileTypePolicySaveFrom {

    @NotBlank(message = "文件类型编码不能为空")
    @Size(max = 80, message = "文件类型编码不能超过80个字符")
    @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_\\-]*$", message = "文件类型编码格式不正确")
    private String code;

    @NotBlank(message = "文件类型名称不能为空")
    @Size(max = 120, message = "文件类型名称不能超过120个字符")
    private String displayName;

    @NotNull(message = "允许扩展名不能为空")
    private JsonNode allowedExtensions;

    @NotNull(message = "允许媒体类型不能为空")
    private JsonNode allowedContentTypes;

    @NotNull(message = "魔数规则不能为空")
    private JsonNode magicRules;

    @NotNull(message = "文件大小上限不能为空")
    @PositiveOrZero(message = "文件大小上限不能为负数")
    private Long maxSize;

    @NotNull(message = "预览开关不能为空")
    private Boolean previewEnabled;

    @NotNull(message = "下载开关不能为空")
    private Boolean downloadEnabled;

    @NotNull(message = "上传开关不能为空")
    private Boolean uploadEnabled;

    @NotNull(message = "危险类型开关不能为空")
    private Boolean dangerous;

    @NotNull(message = "启用开关不能为空")
    private Boolean enabled;

    private Long version;
}
