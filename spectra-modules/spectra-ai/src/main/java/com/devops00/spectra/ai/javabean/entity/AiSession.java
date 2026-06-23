/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
 * @author yangxj96
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
