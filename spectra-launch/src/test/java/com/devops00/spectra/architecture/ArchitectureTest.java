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

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * 后端分层和模块依赖架构测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/24
 */
@AnalyzeClasses(packages = "com.devops00.spectra", importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule CONTROLLERS_MUST_NOT_ACCESS_DATA_ACCESS = noClasses()
            .that()
            .resideInAnyPackage("..controller..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..mapper..", "..repository..");

    @ArchTest
    static final ArchRule SERVICES_MUST_NOT_DEPEND_ON_CONTROLLERS = noClasses()
            .that()
            .resideInAnyPackage("..service..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..controller..");

    @ArchTest
    static final ArchRule DATA_ACCESS_MUST_NOT_DEPEND_ON_CONTROLLERS = noClasses()
            .that()
            .resideInAnyPackage("..mapper..", "..repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..controller..");

}
