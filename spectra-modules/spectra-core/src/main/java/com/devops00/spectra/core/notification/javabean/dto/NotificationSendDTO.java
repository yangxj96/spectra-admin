package com.devops00.spectra.core.notification.javabean.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/// 消息发送DTO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationSendDTO {

    @NotBlank(message = "消息标题不能为空")
    private String title;

    private String content;

    @NotBlank(message = "消息类型不能为空")
    private String type;

    private UUID senderId;

    private String senderName;

    private String link;

    @NotNull(message = "接收者ID不能为空")
    private UUID receiverId;

    private Map<String, Object> extra;
}
