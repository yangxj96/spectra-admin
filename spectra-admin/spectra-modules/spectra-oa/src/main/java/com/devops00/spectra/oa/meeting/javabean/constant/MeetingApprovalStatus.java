package com.devops00.spectra.oa.meeting.javabean.constant;


import com.baomidou.mybatisplus.annotation.IEnum;
import lombok.Getter;

/**
 * 会议状态
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/30 14:30
 */
@Getter
public enum MeetingApprovalStatus implements IEnum<String> {

    DRAFT("draft", "草稿"),
    PROCESSING("processing", "审批中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回"),
    CANCELLED("cancelled", "已撤销");

    /// 状态
    private final String code;

    /// 说明
    private final String name;

    MeetingApprovalStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getValue() {
        return this.code;
    }
}
