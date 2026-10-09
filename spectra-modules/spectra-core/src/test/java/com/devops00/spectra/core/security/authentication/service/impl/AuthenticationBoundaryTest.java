package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authentication.exception.LoginException;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.authentication.javabean.from.LoginFrom;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthenticationBoundaryTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @SuppressWarnings("unchecked")
    void successfulLoginClearsPresentedIdentityInsteadOfAccountUsername() {
        var dispatcher = mock(LoginDispatcher.class);
        var authentication = mock(SecurityAuthenticationPort.class);
        var service = new LoginServiceImpl(dispatcher, (ObjectProvider<AuditService>) mock(ObjectProvider.class),
                authentication, mock(SecurityContextAccessor.class), mock(AuditRecordFactory.class));
        var user = new SecurityUser();
        user.setUsername("account-name");
        var request = new LoginFrom();
        request.setUsername(" Person@Example.TEST ");
        when(dispatcher.authenticate(request)).thenReturn(new UsernamePasswordAuthenticationToken(user, null));
        service.login(request, ClientType.WEB);
        verify(authentication).clearLoginFail(AuthenticationIdentifierHash.digest("person@example.test"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void lockedIdentifierVariantCannotReachAuthentication() {
        var dispatcher = mock(LoginDispatcher.class);
        var authentication = mock(SecurityAuthenticationPort.class);
        var service = new LoginServiceImpl(dispatcher, (ObjectProvider<AuditService>) mock(ObjectProvider.class),
                authentication, mock(SecurityContextAccessor.class), mock(AuditRecordFactory.class));
        when(authentication.isLockedOut(AuthenticationIdentifierHash.digest("person@example.test"))).thenReturn(true);
        var request = new LoginFrom();
        request.setUsername(" Person@Example.TEST ");
        assertThrows(LoginException.class, () -> service.login(request, ClientType.WEB));
        verifyNoInteractions(dispatcher);
    }

    @Test
    @SuppressWarnings("unchecked")
    void equivalentLoginIdentifiersShareFailureBucket() {
        var dispatcher = mock(LoginDispatcher.class);
        var authentication = mock(SecurityAuthenticationPort.class);
        var auditProvider = (ObjectProvider<AuditService>) mock(ObjectProvider.class);
        var service = new LoginServiceImpl(dispatcher, auditProvider, authentication,
                mock(SecurityContextAccessor.class), mock(AuditRecordFactory.class));
        when(dispatcher.authenticate(any())).thenThrow(new LoginException("invalid"));
        for (var identifier : List.of("User@Example.com", " user@example.COM ")) {
            var request = new LoginFrom();
            request.setUsername(identifier);
            assertThrows(LoginException.class, () -> service.login(request, ClientType.WEB));
        }
        var bucket = AuthenticationIdentifierHash.digest("user@example.com");
        verify(authentication, times(2)).isLockedOut(bucket);
        verify(authentication, times(2)).recordLoginFail(bucket);
    }

    @Test
    void anonymousRecipientLookupFailsBeforeReadingUserContacts() {
        var users = mock(UserService.class);
        var contacts = mock(UserContactService.class);
        var directory = new CoreNotificationRecipientDirectory(users, contacts,
                mock(AuthorizationSnapshotProvider.class), mock(DepartmentService.class),
                mock(SecurityContextAccessor.class), mock(UserDepartmentMembershipMapper.class));
        assertThrows(AccessDeniedException.class, () -> directory.resolve(List.of(UUID.randomUUID())));
        verifyNoInteractions(users, contacts);
    }
}
