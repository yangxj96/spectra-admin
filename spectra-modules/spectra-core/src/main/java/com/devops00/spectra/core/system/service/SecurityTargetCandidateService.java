/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.javabean.vo.SecurityUserCandidateVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityVerificationCandidateVO;

import java.util.List;

/** 安全运行态操作目标候选查询服务。 */
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
