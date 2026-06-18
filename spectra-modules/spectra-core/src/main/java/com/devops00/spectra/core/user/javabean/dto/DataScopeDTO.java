package com.devops00.spectra.core.user.javabean.dto;


import com.devops00.spectra.common.constant.DataScopeType;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * 数据范围DTO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/5/13 16:43
 */
@Data
public class DataScopeDTO {

    /**
     * 数据范围类型
     */
    DataScopeType scope;

    /**
     * 部门目标ID
     */
    List<UUID> targets;

}
