package com.devops00.spectra.oa.common.security;

import com.devops00.spectra.common.port.directory.DirectoryQueryPort;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the current user's primary and active associated departments for OA visibility checks. */
@Component
@RequiredArgsConstructor
public class CurrentDepartmentMemberships {

    private final SecurityContextAccessor securityContextAccessor;

    private final DirectoryQueryPort directoryQueryPort;

    public List<UUID> departmentIds() {
        var userId = securityContextAccessor.currentUserId();
        return userId == null ? List.of() : directoryQueryPort.findDepartmentIdsByUserId(userId);
    }

    public boolean contains(UUID departmentId) {
        return departmentId != null && departmentIds().contains(departmentId);
    }
}
