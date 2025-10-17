/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.javabean.user.vo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线用户VO
 *
 * @param account      登录账号
 * @param username     用户名
 * @param organization 组织机构
 * @param loginRecord  登录记录
 */
public record UserOnlineVO(
        String account,
        String username,
        String organization,
        List<LoginRecordVo> loginRecord
) {

    /**
     * 登录记录
     *
     * @param token      令牌
     * @param deviceType 设备类型
     * @param ip         登录IP
     * @param address    登录地址
     * @param createTime 创建时间
     */
    public record LoginRecordVo(
            String token,
            String deviceType,
            String ip,
            String address,
            LocalDateTime createTime
    ) {
    }

}
