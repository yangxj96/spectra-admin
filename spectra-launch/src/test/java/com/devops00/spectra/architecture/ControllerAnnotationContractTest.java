/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.architecture;

import com.devops00.spectra.common.audit.Audit;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Core 与 OA Controller 的接口元数据和构造器注入契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
@AnalyzeClasses(packages = "com.devops00.spectra", importOptions = DoNotIncludeTests.class)
class ControllerAnnotationContractTest {

    private static final String CORE_PACKAGE = "com.devops00.spectra.core.";

    private static final String OA_PACKAGE = "com.devops00.spectra.oa.";

    @ArchTest
    static void controllerEndpointsMustDeclareContractMetadata(JavaClasses classes) {
        var violations = new ArrayList<String>();
        classes.stream()
                .filter(ControllerAnnotationContractTest::isTargetController)
                .forEach(javaClass -> inspectEndpoints(javaClass, violations));

        assertThat(violations)
                .as("Controller endpoint 元数据缺口")
                .isEmpty();
    }

    @ArchTest
    static void controllerDependenciesMustUsePrivateFinalFields(JavaClasses classes) {
        var violations = classes.stream()
                .filter(ControllerAnnotationContractTest::isTargetController)
                .flatMap(javaClass -> Arrays.stream(javaClass.reflect().getDeclaredFields())
                        .filter(ControllerAnnotationContractTest::isInjectedField)
                        .filter(field -> !Modifier.isPrivate(field.getModifiers())
                                || !Modifier.isFinal(field.getModifiers()))
                        .map(field -> javaClass.getName() + "#" + field.getName()
                                + " 必须使用 private final 构造器注入"))
                .toList();

        assertThat(violations)
                .as("Controller 依赖字段必须使用构造器注入")
                .isEmpty();
    }

    private static boolean isTargetController(JavaClass javaClass) {
        var packageName = javaClass.getPackageName();
        return javaClass.isAnnotatedWith(RestController.class)
                && (packageName.startsWith(CORE_PACKAGE) || packageName.startsWith(OA_PACKAGE))
                && packageName.contains(".controller");
    }

    private static boolean isInjectedField(Field field) {
        return !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic();
    }

    private static void inspectEndpoints(JavaClass javaClass, List<String> violations) {
        for (var method : javaClass.reflect().getDeclaredMethods()) {
            var mapping = mappingAnnotation(method);
            if (mapping == null) {
                continue;
            }
            var location = javaClass.getName() + "#" + method.getName();
            if (!mappingVersion(mapping).equals("1.0.0")) {
                violations.add(location + " 缺少 version=\"1.0.0\"");
            }
            if (!method.isAnnotationPresent(Audit.class)) {
                violations.add(location + " 缺少 @Audit");
            }
            if (!hasPreAuthorize(javaClass.reflect(), method)) {
                violations.add(location + " 缺少 @PreAuthorize");
            }
            if (isWriteMapping(mapping)
                    && hasRequestBody(method)
                    && !hasValidated(javaClass.reflect(), method)) {
                violations.add(location + " 存在请求体但缺少 @Validated");
            }
        }
    }

    private static Annotation mappingAnnotation(Method method) {
        for (var annotationType : List.of(GetMapping.class, PostMapping.class,
                PutMapping.class, DeleteMapping.class)) {
            var annotation = method.getAnnotation(annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static String mappingVersion(Annotation mapping) {
        if (mapping instanceof GetMapping annotation) {
            return annotation.version();
        }
        if (mapping instanceof PostMapping annotation) {
            return annotation.version();
        }
        if (mapping instanceof PutMapping annotation) {
            return annotation.version();
        }
        if (mapping instanceof DeleteMapping annotation) {
            return annotation.version();
        }
        throw new IllegalArgumentException("不支持的 Controller mapping 注解: " + mapping.annotationType());
    }

    private static boolean isWriteMapping(Annotation mapping) {
        return mapping instanceof PostMapping
                || mapping instanceof PutMapping
                || mapping instanceof DeleteMapping;
    }

    private static boolean hasRequestBody(Method method) {
        return Arrays.stream(method.getParameterAnnotations())
                .flatMap(Arrays::stream)
                .anyMatch(annotation -> annotation.annotationType().equals(RequestBody.class));
    }

    private static boolean hasPreAuthorize(Class<?> controller, Method method) {
        return method.isAnnotationPresent(PreAuthorize.class)
                || controller.isAnnotationPresent(PreAuthorize.class);
    }

    private static boolean hasValidated(Class<?> controller, Method method) {
        return method.isAnnotationPresent(Validated.class)
                || controller.isAnnotationPresent(Validated.class)
                || Arrays.stream(method.getParameterAnnotations())
                        .flatMap(Arrays::stream)
                        .anyMatch(annotation -> annotation.annotationType().equals(Validated.class));
    }
}
