package com.devops00.spectra.core.quartz.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class QuartzAdminControllerAuthorizationTest {

    @Test
    void everyQuartzEndpointIsRestrictedToDevOps() {
        var endpoints = Arrays.stream(QuartzAdminController.class.getDeclaredMethods())
                .filter(QuartzAdminControllerAuthorizationTest::isEndpoint)
                .toList();

        assertEquals(12, endpoints.size(), "Quartz endpoint inventory changed; update the role policy test");
        for (Method endpoint : endpoints) {
            var preAuthorize = endpoint.getAnnotation(PreAuthorize.class);
            assertNotNull(preAuthorize, endpoint.getName() + " must have an explicit role gate");
            assertEquals("hasRole('ROLE_DEV_OPS')", preAuthorize.value(), endpoint.getName());
        }
    }

    private static boolean isEndpoint(Method method) {
        return method.isAnnotationPresent(GetMapping.class)
                || method.isAnnotationPresent(PostMapping.class)
                || method.isAnnotationPresent(PutMapping.class)
                || method.isAnnotationPresent(DeleteMapping.class);
    }
}
