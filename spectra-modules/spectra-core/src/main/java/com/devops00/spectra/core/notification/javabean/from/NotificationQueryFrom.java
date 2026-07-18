package com.devops00.spectra.core.notification.javabean.from;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 消息分页查询入参
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationQueryFrom {

    /// 消息类型：system/workflow/oa/inner_mail/approval
    private String type;

    /// 是否已读
    private Boolean isRead;

    /// 关键词搜索（标题、内容）
    private String keyword;

    /// 开始时间
    private String startTime;

    /// 结束时间
    private String endTime;
}
