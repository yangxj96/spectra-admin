package com.devops00.spectra.core.javabean.system.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/// 区域路径响应VO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegionPathVO {

    private UUID id;

    private String name;
}