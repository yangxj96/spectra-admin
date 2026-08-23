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

package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.javabean.entity.NotificationSendPreviewEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;

/**
 * 受控发送短时 Preview Mapper。
 */
@Mapper
public interface NotificationSendPreviewMapper extends BaseMapper<NotificationSendPreviewEntity> {

    /**
     * 物理删除过期或已消费的短时快照，避免保留受众范围和参数。
     */
    @Delete("""
            DELETE FROM spectra_notification.ntf_send_preview
             WHERE expires_at < #{now}
                OR (status = 'APPLIED' AND consumed_at < #{appliedCutoff})
            """)
    int deleteExpired(@Param("now") Instant now, @Param("appliedCutoff") Instant appliedCutoff);
}
