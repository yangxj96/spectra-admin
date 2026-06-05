package com.devops00.spectra.ai.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * AI模块Session记录存储
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/6/5 17:10
 */
@Getter
@Setter
@ToString
@TableName(value = "ai_session")
public class AiSession extends BaseEntity {

    /// session_id
    @TableField("session_id")
    private String sessionId;


    /// state_key
    @TableField("state_key")
    private String stateKey;


    /// item_index
    @TableField("item_index")
    private Integer itemIndex;


    /// stateData
    @TableField("state_data")
    private String stateData;

}
