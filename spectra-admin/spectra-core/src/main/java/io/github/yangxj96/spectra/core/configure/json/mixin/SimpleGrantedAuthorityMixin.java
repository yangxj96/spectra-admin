package io.github.yangxj96.spectra.core.configure.json.mixin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SpringSecurity用到的MixIn 类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/3 00:22
 */
public abstract class SimpleGrantedAuthorityMixin {

    @JsonCreator
    public SimpleGrantedAuthorityMixin(@JsonProperty("authority") String role) {}

}
