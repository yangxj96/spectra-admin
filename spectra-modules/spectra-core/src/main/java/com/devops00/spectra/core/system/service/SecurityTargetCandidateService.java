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

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.javabean.vo.SecurityUserCandidateVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityVerificationCandidateVO;

import java.util.List;

/**
 * 安全运行态操作目标候选查询服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecurityTargetCandidateService {

    /**
     * 查询会话和登录失败锁定共用的用户候选。
     *
     * @param keyword 用户编号、用户名或显示名称关键字。
     * @return 脱敏后的用户候选列表。
     */
    List<SecurityUserCandidateVO> searchUserCandidates(String keyword);

    /**
     * 按验证码类型查询可安全定位的联系方式候选。
     *
     * @param type    验证码渠道类型。
     * @param keyword 账号、会话句柄或联系方式关键字。
     * @return 脱敏后的验证码定位候选列表。
     */
    List<SecurityVerificationCandidateVO> searchVerificationCandidates(SecurityVerificationType type, String keyword);
}
