/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.service.impl.SecurityTargetCandidateServiceImpl;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityTargetCandidateServiceTest {

    @Test
    void shouldMapUserCandidatesAndUseTheBoundedDomainQuery() {
        var userService = mock(UserService.class);
        var contactService = mock(UserContactService.class);
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setUsername("alice");
        user.setRealName("Alice");
        user.setEmployeeNo("E001");
        when(userService.searchSecurityCandidates("alice", 20)).thenReturn(List.of(user));
        var service = new SecurityTargetCandidateServiceImpl(userService, contactService);

        var result = service.searchUserCandidates(" alice ");

        assertEquals(1, result.size());
        assertEquals(userId, result.getFirst().id());
        assertEquals("alice", result.getFirst().username());
        assertEquals("Alice", result.getFirst().realName());
        assertEquals("E001", result.getFirst().employeeNo());
        verify(userService).searchSecurityCandidates("alice", 20);
    }

    @Test
    void shouldFindVerificationContactsByTypeAndMaskTheDisplayValue() {
        var userService = mock(UserService.class);
        var contactService = mock(UserContactService.class);
        var userId = UUID.randomUUID();
        var user = new User();
        user.setId(userId);
        user.setUsername("alice");
        user.setRealName("Alice");
        var contact = new UserContact();
        contact.setUserId(userId);
        contact.setContactType(UserContactService.PHONE);
        contact.setContactValue("13800000000");
        when(contactService.searchActiveByType(UserContactService.PHONE, "138", 20)).thenReturn(List.of(contact));
        when(userService.listByIds(List.of(userId))).thenReturn(List.of(user));
        var service = new SecurityTargetCandidateServiceImpl(userService, contactService);

        var result = service.searchVerificationCandidates(SecurityVerificationType.LOGIN_SMS, " 138 ");

        assertEquals(1, result.size());
        assertEquals("13800000000", result.getFirst().target());
        assertEquals("138****0000", result.getFirst().maskedTarget());
        assertEquals(userId, result.getFirst().userId());
        assertEquals("alice", result.getFirst().username());
        verify(contactService).searchActiveByType(UserContactService.PHONE, "138", 20);
    }

    @Test
    void shouldNeverEnumerateKaptchaHandles() {
        var userService = mock(UserService.class);
        var contactService = mock(UserContactService.class);
        var service = new SecurityTargetCandidateServiceImpl(userService, contactService);

        assertEquals(List.of(), service.searchVerificationCandidates(SecurityVerificationType.KAPTCHA, "handle"));

        verify(contactService, never()).searchActiveByType(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt());
    }

    @Test
    void shouldRejectBlankAndOverlongKeywords() {
        var service = new SecurityTargetCandidateServiceImpl(mock(UserService.class), mock(UserContactService.class));

        assertThrows(IllegalArgumentException.class, () -> service.searchUserCandidates(" "));
        assertThrows(IllegalArgumentException.class, () -> service.searchUserCandidates("x".repeat(65)));
    }
}
