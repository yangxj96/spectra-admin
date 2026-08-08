/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.leave.javabean.from;

import lombok.Data;

/// 请假提交审批参数。
@Data
public class LeaveSubmitFrom {

    /// 审批人用户名；未填写时兼容单用户环境并回退到申请人。
    private String approverUsername;
}
