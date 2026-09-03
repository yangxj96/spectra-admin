/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.converter;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.vo.FileAssetVO;
import com.devops00.spectra.core.upload.javabean.vo.FileReferenceAdminVO;
import com.devops00.spectra.core.upload.javabean.vo.FileReferenceVO;
import com.devops00.spectra.core.upload.javabean.vo.FileTypePolicyVO;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadAdminVO;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadPartAdminVO;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 文件上传响应 VO 的时间类型和 MapStruct 转换契约测试。 */
class FileUploadConverterTest {

    private static final List<Class<?>> RESPONSE_VOS = List.of(
            FileAssetVO.class,
            FileTypePolicyVO.class,
            FileUploadAdminVO.class,
            FileUploadPartAdminVO.class,
            FileReferenceAdminVO.class,
            FileReferenceVO.class,
            PartTargetVO.class,
            UploadSessionVO.class);

    @Test
    void responseTimeFieldsMustUseUserLocalDateTime() {
        for (Class<?> responseVO : RESPONSE_VOS) {
            for (Field field : responseVO.getDeclaredFields()) {
                if (field.getName().endsWith("At") || field.getName().endsWith("Time")) {
                    assertThat(field.getType())
                            .as("%s.%s", responseVO.getSimpleName(), field.getName())
                            .isEqualTo(LocalDateTime.class);
                }
            }
        }
    }

    @Test
    void mapStructConverterMustApplyCurrentUserTimezone() throws Exception {
        Class<?> converterType = Class.forName(FileUploadConverter.class.getName());

        SecurityContextAccessor accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserZoneId()).thenReturn("Asia/Shanghai");
        var timeMapper = new com.devops00.spectra.framework.configure.mapstruct.TimeMapper(accessor);
        Class<?> implementationType = Class.forName(converterType.getName() + "Impl");
        var converter = implementationType.getConstructor(
                com.devops00.spectra.framework.configure.mapstruct.TimeMapper.class).newInstance(timeMapper);

        FileAsset asset = new FileAsset();
        asset.setCreatedAt(Instant.parse("2026-08-30T16:24:30.765318Z"));
        FileAssetVO vo = (FileAssetVO) implementationType.getMethod("toAssetVO", FileAsset.class)
                .invoke(converter, asset);

        assertThat(vo.getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(
                Instant.parse("2026-08-30T16:24:30.765318Z"), ZoneId.of("Asia/Shanghai")));
    }
}
