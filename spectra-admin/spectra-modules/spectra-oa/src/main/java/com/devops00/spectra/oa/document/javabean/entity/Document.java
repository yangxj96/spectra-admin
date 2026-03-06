package com.devops00.spectra.oa.document.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// OA-文档表主表实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_document")
public class Document extends BaseEntity {
}
