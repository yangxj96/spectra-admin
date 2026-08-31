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

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * 单租户非 SaaS 架构约束测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/31
 */
@AnalyzeClasses(packages = "com.devops00.spectra", importOptions = DoNotIncludeTests.class)
class SingleTenantArchitectureTest {

    private static final Set<String> FORBIDDEN_TYPE_NAMES = Set.of("TenantContext", "TenantResolver", "TenantService");

    @ArchTest
    static final com.tngtech.archunit.lang.ArchRule APPLICATION_MUST_NOT_DEPEND_ON_TENANT_PACKAGES = noClasses()
            .that()
            .resideInAnyPackage("com.devops00.spectra..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..tenant..", "..tenancy..");

    @ArchTest
    static void applicationDoesNotExposeTenantTypesOrFields(JavaClasses classes) {
        var forbiddenTypes = classes.stream()
                .filter(javaClass -> FORBIDDEN_TYPE_NAMES.contains(javaClass.getSimpleName())
                        || containsTenantTerm(javaClass.getPackageName()))
                .map(JavaClass::getName)
                .collect(Collectors.toSet());

        var forbiddenFields = classes.stream()
                .flatMap(javaClass -> javaClass.getFields()
                        .stream()
                        .filter(field -> containsTenantTerm(field.getName()))
                        .map(field -> javaClass.getName() + "." + field.getName()))
                .collect(Collectors.toSet());

        assertThat(forbiddenTypes)
                .as("应用代码不得暴露租户类型或租户包；Flowable 引擎内部字段不属于应用包扫描范围")
                .isEmpty();
        assertThat(forbiddenFields)
                .as("应用业务类不得声明 tenant/tenantId 字段")
                .isEmpty();
    }

    private static boolean containsTenantTerm(String value) {
        return value.toLowerCase(Locale.ROOT).contains("tenant") || value.contains("租户");
    }

}
