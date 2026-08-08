package com.devops00.spectra.core.notification.javabean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/// 批量消息发送DTO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationBatchSendDTO {

    /// 标题。
    @NotBlank(message = "消息标题不能为空")
    private String title;

    /// 内容。
    private String content;

    /// 类型。
    @NotBlank(message = "消息类型不能为空")
    private String type;

    /// 发送人 ID。
    private UUID senderId;

    /// 发送人姓名。
    private String senderName;

    /// 链接字段。
    private String link;

    /// 接收人 ID 列表。
    @NotEmpty(message = "接收者列表不能为空")
    private List<UUID> receiverIds;

    /// 扩展数据。
    private Map<String, Object> extra;
}
