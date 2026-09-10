/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.javabean.vo.SecurityUserCandidateVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityVerificationCandidateVO;

import java.util.List;

/** 安全运行态操作目标候选查询服务。 */
public interface SecurityTargetCandidateService {

    /** 查询会话和登录失败锁定共用的用户候选。 */
    List<SecurityUserCandidateVO> searchUserCandidates(String keyword);

    /** 按验证码类型查询可安全定位的联系方式候选。 */
    List<SecurityVerificationCandidateVO> searchVerificationCandidates(SecurityVerificationType type, String keyword);
}
