package io.github.yangxj96.spectra.security.starter.eval;


import io.github.yangxj96.spectra.security.base.properties.SecurityProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/// 重写验证方法
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/3 11:52
@Slf4j
@Component
@NullMarked
public class SpectraPermissionEvaluator implements PermissionEvaluator {

    @Resource
    private SecurityProperties securityProperties;

    /// 前缀为 ROLE_ 的权限属于“角色”，不参与细粒度权限匹配
    private static final String ROLE_PREFIX = "ROLE_";

    /// LRU 缓存：用于缓存编译后的权限表达式结构，提高匹配性能
    private static final int MAX_CACHE = 500;

    /// 权限表达式缓存（线程安全 LRU）
    ///
    /// key: 原始权限表达式，如 "order:*:read"
    ///
    /// value: 预编译表示，例如分段结构或 regex Pattern
    private static final Map<String, CompiledPermissionPattern> CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(128, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CompiledPermissionPattern> eldest) {
                    return size() > MAX_CACHE;
                }
            });

    /// 预编译后的权限表达式结构
    ///
    /// 若为 segmentBased=true，则使用分段匹配（最快）
    ///
    /// 若为 segmentBased=false，则 fallback 使用 regex
    ///
    /// @param segmentBased
    /// @param segments      仅在 segmentBased=true 时不为空
    /// @param hasDoubleStar 是否包含 "**" 通配
    /// @param regex         仅在 segmentBased=false 时不为空
    private record CompiledPermissionPattern(
            boolean segmentBased,
            String[] segments,
            boolean hasDoubleStar,
            Pattern regex
    ) {
    }

    /// 基于对象实例进行权限判断。
    ///
    /// @param authentication     当前认证对象（@NullMarked → 必定非 null）
    /// @param targetDomainObject 目标领域对象，可为 null（如业务不需要资源对象）
    /// @param permission         表达式权限字符串（非 null）
    /// @return true: 拥有权限 false: 权限不足
    @Override
    public boolean hasPermission(Authentication authentication, @Nullable Object targetDomainObject, Object permission) {
        log.info("进入自己重写的权限鉴定器,{},{}", targetDomainObject, permission);
        // targetDomainObject 是唯一允许为 null 的参数（因业务允许）
        // 若你的业务要求必须有资源对象，则这里直接拒绝
        //if (targetDomainObject == null) {
        //    return false;
        //}

        // 超级管理员/全权限 用户直接放行
        if (hasAbsolutePrivilege(authentication)) {
            return true;
        }

        // permission 在 @NullMarked 下保证非空，可直接使用
        CompiledPermissionPattern compiled = compilePattern(permission.toString());

        // 遍历用户实际拥有的权限
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String userPerm = auth.getAuthority();

            // 跳过角色（形如 ROLE_ADMIN）
            if (userPerm == null || userPerm.startsWith(ROLE_PREFIX)) {
                continue;
            }

            // 尝试匹配
            if (matches(compiled, userPerm)) {
                return true;
            }
        }

        return false;
    }

    /// 基于资源 ID + 类型的权限判断。
    ///
    /// 当前业务无需使用targetId/targetType,因此委托给第一个方法。
    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // targetId / targetType 在 @NullMarked 下必然非 null
        // 但业务不需要他们，因此传入一个“非 null 的 dummy 对象”
        return hasPermission(authentication, new Object(), permission);
    }

    /// 判断用户是否拥有绝对权限:
    ///
    /// * ADMINISTRATORS（即 ROLE_DEV_OPS）
    /// * 或权限字符串为 "*"（全权限）
    private boolean hasAbsolutePrivilege(Authentication authentication) {
        for (var ga : authentication.getAuthorities()) {
            var authority = ga.getAuthority();
            if (securityProperties.getAdministrators().equals(authority) || "*".equals(authority)) {
                return true;
            }
        }
        return false;
    }

    /// 编译权限表达式。
    ///
    /// 如果表达式包含 ":" → 使用分段匹配（性能最佳）
    ///
    /// 否则 → fallback 为 regex
    private static CompiledPermissionPattern compilePattern(String expr) {
        var cached = CACHE.get(expr);
        if (cached != null) {
            return cached;
        }

        CompiledPermissionPattern result;

        if (expr.contains(":")) {
            // 分段权限，如 "order:*:read"
            String[] segments = expr.split(":");
            boolean hasDoubleStar = expr.contains("**");
            result = new CompiledPermissionPattern(true, segments, hasDoubleStar, null);
        } else {
            // URL 或其他结构，使用 regex fallback
            String regex = expr
                    .replace(".", "\\.")
                    .replace("**", ".*")
                    .replace("*", "[^/]+");

            result = new CompiledPermissionPattern(false, null, false, Pattern.compile("^" + regex + "$"));
        }

        CACHE.put(expr, result);
        return result;
    }

    /// 根据编译好的结构选择对应匹配算法
    private static boolean matches(CompiledPermissionPattern cp, String userPerm) {
        if (cp.segmentBased) {
            return matchSegments(cp.segments, cp.hasDoubleStar, userPerm);
        }
        return cp.regex.matcher(userPerm).matches();
    }

    /// 多级权限分段匹配：
    ///
    /// 示例：
    /// ```text
    /// pattern:  order:*:read
    /// user:     order:12:read
    ///
    /// pattern:  order:**            (多级通配)
    /// user:     order:12:item:edit
    /// ```
    private static boolean matchSegments(String[] patternSegs, boolean hasDoubleStar, String userPerm) {
        // userPerm 在 NullMarked 下保证非 null
        var userSegs = userPerm.split(":");

        // 若不包含 **，则长度必须一致
        if (!hasDoubleStar && patternSegs.length != userSegs.length) {
            return false;
        }
        int p = 0;
        int u = 0;
        while (p < patternSegs.length && u < userSegs.length) {
            var seg = patternSegs[p];
            // ** → 多级通配，剩余全部匹配
            if (seg.equals("**")) {
                return true;
            }
            // * → 单段通配
            if (seg.equals("*") || seg.equals(userSegs[u])) {
                p++;
                u++;
                continue;
            }
            // 不匹配
            return false;
        }
        // 完全匹配
        return p == patternSegs.length && u == userSegs.length;
    }
}

