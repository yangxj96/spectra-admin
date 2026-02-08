package io.github.yangxj96.spectra.core.configure.security.javabean;

import lombok.Data;

import java.util.List;

@Data
public class CachedSecurityUser {

    private String id;
    private String name;
    private String email;
    private String avatar;
    private String organizationId;
    private Short state;
    private boolean enabled;

    private List<String> authorities;
}
