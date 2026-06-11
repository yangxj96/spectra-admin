package com.devops00.spectra.core.javabean.system.vo;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/// 区域路径响应VO
@Data
public class RegionPathVO {

    private List<UUID> ids;

    private List<String> names;

    private String fullName;
}