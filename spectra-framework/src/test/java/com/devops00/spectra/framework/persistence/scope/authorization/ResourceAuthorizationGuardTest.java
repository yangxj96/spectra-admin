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

package com.devops00.spectra.framework.persistence.scope.authorization;

import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.ExecutionContext;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ResourceOperation;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.common.security.authorization.ScopeQuery;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 资源授权门禁构造和批量快照回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class ResourceAuthorizationGuardTest {

    private static final UUID USER_ID = UUID.fromString("019bdfdd-b58d-7232-943f-af4141801ae3");

    @Test
    void shouldUseImplicitConstructorInjection() {
        var constructors = ResourceAuthorizationGuard.class.getDeclaredConstructors();

        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getAnnotation(Autowired.class)).isNull();
    }

    @Test
    void shouldLoadOneAuthorizationSnapshotForTheWholeBatch() {
        var snapshotProvider = mock(AuthorizationSnapshotProvider.class);
        when(snapshotProvider.load(USER_ID)).thenReturn(snapshot());
        ObjectProvider<AuthorizationSnapshotProvider> provider = mock();
        when(provider.getIfAvailable()).thenReturn(snapshotProvider);
        var guard = new ResourceAuthorizationGuard(provider);
        var context = ExecutionContext.of(USER_ID, "resource", ResourceOperation.BATCH);
        var query = new ScopeQuery(USER_ID, USER_ID, null, Set.of());

        guard.assertBatchAllowed(context, List.of(query, query));

        verify(snapshotProvider).load(USER_ID);
    }

    /**
     * 处理快照相关数据。
     */
    private static AuthorizationSnapshot snapshot() {
        var boundary = new PermissionBoundary("resource:batch", AuthorizationScope.of(ScopeMode.ALL));
        var assignment = new AuthorizationAssignment(UUID.randomUUID(), "ROLE_USER", 1,
                Map.of(boundary.permission(), boundary), Map.of());
        return AuthorizationSnapshot.of(List.of(assignment));
    }
}
