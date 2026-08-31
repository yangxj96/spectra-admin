/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.common.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

class AuditAnnotationTest {

    @Test
    void auditAnnotationMustBeRuntimeMethodAnnotation() throws NoSuchMethodException {
        assertEquals(RetentionPolicy.RUNTIME, Audit.class.getAnnotation(Retention.class).value());
        assertTrue(Audit.class.getAnnotation(Target.class).value().length == 1);
        assertEquals(ElementType.METHOD, Audit.class.getAnnotation(Target.class).value()[0]);
        assertEquals(AuditCategory.OPERATION, Audit.class.getMethod("category").getDefaultValue());
        assertEquals("", Audit.class.getMethod("eventType").getDefaultValue());
    }
}
