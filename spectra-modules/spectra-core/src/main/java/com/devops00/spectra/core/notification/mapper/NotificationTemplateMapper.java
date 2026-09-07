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

package com.devops00.spectra.core.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.core.notification.javabean.entity.NotificationTemplateEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.UUID;

/**
 * 通知模板 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Mapper
public interface NotificationTemplateMapper extends BaseMapper<NotificationTemplateEntity> {

    /**
     * 按通知批次一次加载各渠道当前可用的已发布模板。
     *
     * @param templateGroupCode  逻辑模板组编码
     * @param purpose            通知用途
     * @param channels           批次实际使用的渠道
     * @param templateVersionIds 受控发送锁定的模板版本 ID；为空时按模板组查询
     * @return 各渠道已发布模板
     */
    List<NotificationTemplateEntity> selectPublishedTemplates(
                                                              @Param("templateGroupCode") String templateGroupCode,
                                                              @Param("purpose") String purpose,
                                                              @Param("channels") List<String> channels,
                                                              @Param("templateVersionIds") List<UUID> templateVersionIds);
}
