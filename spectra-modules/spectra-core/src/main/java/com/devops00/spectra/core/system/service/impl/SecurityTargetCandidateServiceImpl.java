/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.javabean.vo.SecurityUserCandidateVO;
import com.devops00.spectra.core.system.javabean.vo.SecurityVerificationCandidateVO;
import com.devops00.spectra.core.system.service.SecurityTargetCandidateService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 安全运行态目标候选查询实现。 */
@Service
@RequiredArgsConstructor
public class SecurityTargetCandidateServiceImpl implements SecurityTargetCandidateService {

    private static final int CANDIDATE_LIMIT = 20;
    private static final int MAX_KEYWORD_LENGTH = 64;

    private final UserService userService;
    private final UserContactService contactService;

    @Override
    public List<SecurityUserCandidateVO> searchUserCandidates(String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        return userService.searchSecurityCandidates(normalizedKeyword, CANDIDATE_LIMIT).stream()
                .map(user -> new SecurityUserCandidateVO(
                        user.getId(), user.getUsername(), user.getRealName(), user.getEmployeeNo()))
                .toList();
    }

    @Override
    public List<SecurityVerificationCandidateVO> searchVerificationCandidates(SecurityVerificationType type,
                                                                               String keyword) {
        String normalizedKeyword = normalizeKeyword(keyword);
        String contactType = contactType(type);
        if (contactType == null) {
            return List.of();
        }
        List<UserContact> contacts = contactService.searchActiveByType(contactType, normalizedKeyword, CANDIDATE_LIMIT);
        if (contacts.isEmpty()) {
            return List.of();
        }
        List<UUID> userIds = contacts.stream().map(UserContact::getUserId).filter(id -> id != null).distinct().toList();
        Map<UUID, User> users = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity(), (first, ignored) -> first));
        return contacts.stream()
                .map(contact -> toVerificationCandidate(contact, users.get(contact.getUserId())))
                .filter(candidate -> candidate != null)
                .toList();
    }

    private static SecurityVerificationCandidateVO toVerificationCandidate(UserContact contact, User user) {
        if (user == null || contact.getUserId() == null || contact.getContactValue() == null
                || contact.getContactValue().isBlank()) {
            return null;
        }
        String target = contact.getContactValue();
        return new SecurityVerificationCandidateVO(target, maskTarget(contact.getContactType(), target), user.getId(),
                user.getUsername(), user.getRealName(), user.getEmployeeNo());
    }

    private static String contactType(SecurityVerificationType type) {
        if (type == null) {
            throw new IllegalArgumentException("验证码类型不能为空");
        }
        return switch (type) {
            case LOGIN_SMS, BIND_PHONE -> UserContactService.PHONE;
            case LOGIN_EMAIL, BIND_EMAIL -> UserContactService.EMAIL;
            case KAPTCHA -> null;
        };
    }

    private static String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank() || keyword.trim().length() > MAX_KEYWORD_LENGTH) {
            throw new IllegalArgumentException("候选查询关键字不能为空且长度不能超过 64");
        }
        return keyword.trim();
    }

    private static String maskTarget(String contactType, String target) {
        if (UserContactService.PHONE.equals(contactType)) {
            return maskPhone(target);
        }
        int at = target.indexOf('@');
        if (at > 1) {
            String local = target.substring(0, at);
            return local.charAt(0) + "***" + local.charAt(local.length() - 1) + target.substring(at);
        }
        return maskMiddle(target);
    }

    private static String maskPhone(String target) {
        if (target.length() <= 7) {
            return maskMiddle(target);
        }
        return target.substring(0, 3) + "****" + target.substring(target.length() - 4);
    }

    private static String maskMiddle(String target) {
        if (target.length() <= 2) {
            return "*".repeat(target.length());
        }
        return target.charAt(0) + "***" + target.charAt(target.length() - 1);
    }
}
