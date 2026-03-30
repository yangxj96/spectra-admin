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
public enum MeetingStatus implements IEnum<String> {

    DRAFT("draft", "草稿"),
    SCHEDULED("scheduled", "已安排"),
    ONGOING("ongoing", "进行中"),
    FINISHED("finished", "已结束"),
    CANCELLED("cancelled", "已取消");


    /// 状态
    private final String code;

    /// 说明
    private final String name;

    MeetingStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    @Override
    public String getValue() {
        return this.code;
    }
}
