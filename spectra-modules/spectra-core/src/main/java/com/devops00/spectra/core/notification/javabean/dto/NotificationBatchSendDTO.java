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

    @NotBlank(message = "消息标题不能为空")
    private String title;

    private String content;

    @NotBlank(message = "消息类型不能为空")
    private String type;

    private UUID senderId;

    private String senderName;

    private String link;

    @NotEmpty(message = "接收者列表不能为空")
    private List<UUID> receiverIds;

    private Map<String, Object> extra;
}
