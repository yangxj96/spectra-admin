package io.github.yangxj96.spectra.security.method.autoconfigure;

import io.github.yangxj96.spectra.security.api.checker.PermissionChecker;
import io.github.yangxj96.spectra.security.method.authorization.HasPermissionAuthorizationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SpectraMethodSecurityAutoConfiguration {

    @Bean
    public HasPermissionAuthorizationManager hasPermissionAuthorizationManager(PermissionChecker permissionChecker) {
        return new HasPermissionAuthorizationManager(permissionChecker);
    }
}
