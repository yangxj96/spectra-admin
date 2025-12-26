package io.github.yangxj96.spectra.core.configure.json.mixin;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * SpringSecurity用到的MixIn 类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/3 00:22
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("unused")
public abstract class SimpleGrantedAuthorityMixin {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    protected SimpleGrantedAuthorityMixin(@JsonProperty("authority") String role) {
    }

}
