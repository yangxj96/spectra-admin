package com.devops00.spectra.oa.asset.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.*;

/// OA-资产表主表实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_asset")
public class Asset extends BaseEntity {
}
