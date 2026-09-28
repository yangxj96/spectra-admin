package com.devops00.spectra.launch.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuiltinRoleAuthorizationDefaultsTest {

    private static final Pattern ROLE_PERMISSION_ROW = Pattern.compile(
            "\\('(ROLE_ADMIN_SYSTEM|ROLE_AUDIT|ROLE_USER)', '([a-z0-9_-]+(?::[a-z0-9_-]+)+)'\\)");
    private static final Pattern ROLE_MENU_ROW = Pattern.compile(
            "\\('(ROLE_ADMIN_SYSTEM|ROLE_AUDIT|ROLE_USER)', '([A-Za-z][A-Za-z0-9]+)'\\)");
    private static final Pattern GRANTABLE_EXCLUSIONS = Pattern.compile("permission_code NOT IN \\((?<codes>[^)]*)\\)");
    private static final Pattern SQL_STRING = Pattern.compile("'([^']+)'");

    @Test
    void migrationDefinesTheExactAdminAndAuditorPermissionSets() throws IOException {
        var sql = migration();
        var grants = collect(sql, ROLE_PERMISSION_ROW);

        assertEquals(Set.of(
                "user:create", "user:read", "user:update", "user:disable", "user:reset-password",
                "user:unlock", "user:assign-role", "role:assign", "role:create", "role:read", "role:update",
                "role:delete", "role:disable", "role:grant", "role:authority-level:update", "permission:read",
                "menu:create", "menu:read", "menu:update", "menu:disable", "department:create", "department:read",
                "department:update", "dictionary:create", "dictionary:read", "dictionary:update",
                "dictionary:disable", "region:read", "oa:application:read", "oa:application:update", "oa:asset:create",
                "oa:asset:read", "oa:asset:update", "oa:contact:read", "oa:contract:create", "oa:contract:delete",
                "oa:contract:read", "oa:contract:update", "oa:document:create", "oa:document:read", "oa:document:update",
                "oa:leave:read", "oa:leave:update", "oa:meeting:create", "oa:meeting:read", "oa:meeting:update",
                "oa:notice:create", "oa:notice:read", "oa:notice:update", "oa:purchase:create", "oa:purchase:read",
                "oa:purchase:update", "oa:reimbursement:read", "oa:reimbursement:update", "oa:report:read",
                "oa:application-type:create", "oa:application-type:read", "oa:application-type:update",
                "oa:application-type:disable", "workflow:process:create", "workflow:process:read", "workflow:process:update",
                "workflow:instance:read", "workflow:instance:update", "workflow:task:read", "workflow:task:update"),
                grants.get("ROLE_ADMIN_SYSTEM"));

        assertEquals(Set.of(
                "audit:read", "audit:export", "user:read", "role:read", "permission:read", "menu:read",
                "department:read", "dictionary:read", "region:read", "file:read", "oa:application:read",
                "oa:asset:read", "oa:contact:read", "oa:contract:read", "oa:document:read", "oa:leave:read",
                "oa:meeting:read", "oa:notice:read", "oa:purchase:read", "oa:reimbursement:read", "oa:report:read",
                "oa:application-type:read", "workflow:process:read", "workflow:instance:read",
                "workflow:task:read", "notification:provider:read", "notification:template:read"),
                grants.get("ROLE_AUDIT"));

        assertEquals(Set.of(
                "account:read", "account:update", "notification-setting:read", "notification-setting:update",
                "notification:read", "notification:update", "notification:delete", "oa:calendar:create",
                "oa:calendar:read", "oa:calendar:update", "oa:calendar:delete", "oa:workbench:read",
                "workflow:instance:create"), grants.get("ROLE_USER"));

        assertTrue(sql.contains("ROLE_DEV_OPS"), "the migration must preserve the Root role contract");
    }

    @Test
    void migrationDefinesTheExactNonRootMenuSets() throws IOException {
        var menus = collect(migration(), ROLE_MENU_ROW);

        assertEquals(Set.of(
                "Dashboard", "SystemUser", "SystemRoleManagement", "SystemDept", "SystemDict", "SystemMenu",
                "SystemWorkflow", "SystemRegion", "OAAsset", "OASupply", "OALeave", "OAApplicationTypes",
                "OAContact", "OAContract", "OADocument", "OAMeeting", "OANotice", "OAPurchase",
                "OAReimbursement", "OAReport", "OAApproval", "OAApprovalReimbursement", "OAApprovalPurchase",
                "OAApprovalLeave"), menus.get("ROLE_ADMIN_SYSTEM"));
        assertEquals(Set.of(
                "Dashboard", "SystemUser", "SystemRoleManagement", "SystemDept", "SystemDict", "SystemMenu",
                "SystemWorkflow", "SystemRegion", "DevopsAuditLog", "DevopsNotificationTemplate",
                "DevopsNotificationProvider", "OAAsset", "OASupply", "OALeave", "OAApplicationTypes", "OAContact",
                "OAContract", "OADocument", "OAMeeting", "OANotice", "OAPurchase", "OAReimbursement", "OAReport",
                "OAApproval", "OAApprovalReimbursement", "OAApprovalPurchase", "OAApprovalLeave"),
                menus.get("ROLE_AUDIT"));
        assertEquals(Set.of("Dashboard", "OACalendar"), menus.get("ROLE_USER"));
    }

    @Test
    void adminMayDelegateOnlyItsOwnNonControlPermissions() throws IOException {
        var sql = migration();
        var excludedMatcher = GRANTABLE_EXCLUSIONS.matcher(sql);
        assertTrue(excludedMatcher.find(), "Admin grantable permissions must exclude role and assignment controls");

        var exclusions = new HashSet<String>();
        var stringMatcher = SQL_STRING.matcher(excludedMatcher.group("codes"));
        while (stringMatcher.find()) exclusions.add(stringMatcher.group(1));
        assertEquals(Set.of(
                "user:assign-role", "role:assign", "role:create", "role:update", "role:delete", "role:disable",
                "role:grant", "role:authority-level:update"), exclusions);
        assertTrue(sql.contains("WHERE baseline.role_code = 'ROLE_ADMIN_SYSTEM'"),
                "only the system administrator may have default grantable permissions");
        assertTrue(sql.contains("_builtin_role_permission_baseline"),
                "grantable permissions must be a subset of the direct role permission baseline");
    }

    private static Map<String, Set<String>> collect(String sql, Pattern pattern) {
        var result = new HashMap<String, Set<String>>();
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            result.computeIfAbsent(matcher.group(1), ignored -> new HashSet<>()).add(matcher.group(2));
        }
        return result;
    }

    private static String migration() throws IOException {
        var resource = BuiltinRoleAuthorizationDefaultsTest.class.getResourceAsStream(
                "/db/migration/V8__align_builtin_role_authorization_defaults.sql");
        assertNotNull(resource, "Flyway V8 migration must be present on the application classpath");
        try (resource) {
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
