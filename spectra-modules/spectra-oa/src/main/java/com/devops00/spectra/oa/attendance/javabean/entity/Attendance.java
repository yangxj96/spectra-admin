package com.devops00.spectra.oa.attendance.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// OA-考勤表主表实体
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:53
@Getter
@Setter
@ToString
@TableName(value = "oa_attendance")
public class Attendance extends BaseEntity {
}
