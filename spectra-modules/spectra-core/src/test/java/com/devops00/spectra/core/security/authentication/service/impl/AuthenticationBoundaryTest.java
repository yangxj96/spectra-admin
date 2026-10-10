package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.notification.NotificationSystemActor;
import com.devops00.spectra.common.port.security.SecurityAuthenticationPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authentication.exception.LoginException;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.authentication.javabean.from.LoginFrom;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        assertEquals(List.of(), directory.resolve(List.of()));
        assertThrows(AccessDeniedException.class, () -> directory.resolve(List.of(UUID.randomUUID())));
        assertThrows(AccessDeniedException.class, () -> directory.resolveByLoginNames(List.of("assignee")));
        verifyNoInteractions(users, contacts);
    }

    @Test
    void systemRecipientLookupRequiresRegisteredIdentityAndExplicitLoginNameScope() {
        var users = mock(UserService.class);
        var contacts = mock(UserContactService.class);
        var authorization = mock(AuthorizationSnapshotProvider.class);
        var directory = new CoreNotificationRecipientDirectory(users, contacts, authorization,
                mock(DepartmentService.class), mock(SecurityContextAccessor.class),
                mock(UserDepartmentMembershipMapper.class));
        var recipientId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () -> directory.resolveAsSystem(List.of(recipientId), null));
        assertThrows(AccessDeniedException.class, () -> directory.resolveByLoginNamesAsSystem(List.of("assignee"),
                NotificationSystemActor.OA_CONTRACT_REMINDER));
        verifyNoInteractions(users, contacts, authorization);

        var user = mock(User.class);
        when(user.getId()).thenReturn(recipientId);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(users.getByUsername("assignee")).thenReturn(user);
        when(users.getById(recipientId)).thenReturn(user);
        when(contacts.listActiveByUserId(recipientId)).thenReturn(List.of());
        var recipients = directory.resolveByLoginNamesAsSystem(List.of("assignee"),
                NotificationSystemActor.WORKFLOW_TASK);
        assertEquals(1, recipients.size());
        assertEquals(recipientId, recipients.getFirst().userId());
        assertTrue(recipients.getFirst().active());
        assertEquals(recipientId, directory.resolveAsSystem(List.of(recipientId),
                NotificationSystemActor.OA_CONTRACT_REMINDER).getFirst().userId());
        verifyNoInteractions(authorization);
    }

    @Test
    void authenticatedRecipientLookupKeepsCurrentUserScope() {
        var users = mock(UserService.class);
        var contacts = mock(UserContactService.class);
        var authorization = mock(AuthorizationSnapshotProvider.class);
        var context = mock(SecurityContextAccessor.class);
        var memberships = mock(UserDepartmentMembershipMapper.class);
        var directory = new CoreNotificationRecipientDirectory(users, contacts, authorization,
                mock(DepartmentService.class), context, memberships);
        var requesterId = UUID.randomUUID();
        var recipientId = UUID.randomUUID();
        when(context.currentUserId()).thenReturn(requesterId);
        when(authorization.load(requesterId)).thenReturn(AuthorizationSnapshot.of(List.of()));
        when(memberships.selectDepartmentIdsForAuthorization(recipientId)).thenReturn(List.of());
        var user = mock(User.class);
        when(users.getById(recipientId)).thenReturn(user);

        var denied = directory.resolve(List.of(recipientId));
        assertEquals(1, denied.size());
        assertEquals(recipientId, denied.getFirst().userId());
        assertFalse(denied.getFirst().active());
        verifyNoInteractions(contacts);

        var root = AuthorizationSnapshot.of(List.of(new AuthorizationAssignment(UUID.randomUUID(),
                RootAuthorizationPolicy.ROOT_ROLE, 1000, Map.of(), Map.of())));
        when(authorization.load(requesterId)).thenReturn(root);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(contacts.listActiveByUserId(recipientId)).thenReturn(List.of());
        assertTrue(directory.resolve(List.of(recipientId)).getFirst().active());
    }

    @Test
    void concurrentAnonymousAndAuthenticatedLookupsKeepTheirOwnIdentityBoundary() throws Exception {
        var users = mock(UserService.class);
        var contacts = mock(UserContactService.class);
        var authorization = mock(AuthorizationSnapshotProvider.class);
        var context = mock(SecurityContextAccessor.class);
        var identities = new ThreadLocal<UUID>();
        var directory = new CoreNotificationRecipientDirectory(users, contacts, authorization,
                mock(DepartmentService.class), context, mock(UserDepartmentMembershipMapper.class));
        var requesterId = UUID.randomUUID();
        var recipientId = UUID.randomUUID();
        var root = AuthorizationSnapshot.of(List.of(new AuthorizationAssignment(UUID.randomUUID(),
                RootAuthorizationPolicy.ROOT_ROLE, 1000, Map.of(), Map.of())));
        var user = mock(User.class);
        when(context.currentUserId()).thenAnswer(ignored -> identities.get());
        when(authorization.load(requesterId)).thenReturn(root);
        when(users.getById(recipientId)).thenReturn(user);
        when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
        when(contacts.listActiveByUserId(recipientId)).thenReturn(List.of());

        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var anonymous = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(5, TimeUnit.SECONDS));
                return assertThrows(AccessDeniedException.class,
                        () -> directory.resolve(List.of(recipientId))) != null;
            });
            var authenticated = executor.submit(() -> {
                identities.set(requesterId);
                try {
                    ready.countDown();
                    assertTrue(start.await(5, TimeUnit.SECONDS));
                    return directory.resolve(List.of(recipientId)).getFirst().active();
                } finally {
                    identities.remove();
                }
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            assertTrue(anonymous.get(5, TimeUnit.SECONDS));
            assertTrue(authenticated.get(5, TimeUnit.SECONDS));
        }
        verify(users, times(1)).getById(recipientId);
        verify(contacts, times(1)).listActiveByUserId(recipientId);
    }
}
