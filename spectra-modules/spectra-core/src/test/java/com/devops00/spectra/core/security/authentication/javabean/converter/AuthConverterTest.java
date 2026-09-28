package com.devops00.spectra.core.security.authentication.javabean.converter;

import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import com.devops00.spectra.core.user.javabean.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthConverterTest {

    private final AuthConverter converter = Mappers.getMapper(AuthConverter.class);

    @Test
    void mapsPrimaryDepartmentToSecurityPrincipalDepartment() {
        var primaryDepartmentId = UUID.randomUUID();
        var user = new User();
        user.setPrimaryDepartmentId(primaryDepartmentId);

        SecurityUser principal = converter.toSecurityUser(user);

        assertEquals(primaryDepartmentId, principal.getDepartmentId());
    }
}
