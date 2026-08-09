package com.devops00.spectra.core.notification.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 消息响应VO
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    private UUID id;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private String type;

    /**
     * 发送者ID
     */
    private UUID senderId;

    /**
     * 发送者名称
     */
    private String senderName;

    /**
     * 点击跳转路径
     */
    private String link;

    /**
     * 是否已读
     */
    private Boolean isRead;

    /**
     * 阅读时间
     */
    private LocalDateTime readAt;

    /**
     * 接收者ID
     */
    private UUID receiverId;

    /**
     * 扩展数据
     */
    private String extra;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
