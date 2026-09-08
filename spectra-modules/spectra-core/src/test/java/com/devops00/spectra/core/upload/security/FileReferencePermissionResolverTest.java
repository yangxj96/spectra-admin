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

package com.devops00.spectra.core.upload.security;

import com.devops00.spectra.common.port.file.FileReferencePermissionChecker;
import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileReferencePermissionResolverTest {

    private static final String REFERENCE_TYPE = "OA_DOCUMENT_VERSION";

    private static final UUID REFERENCE_ID = UUID.randomUUID();

    private static final UUID USER_ID = UUID.randomUUID();

    @Test
    void rejectsBlankReferenceTypeWithoutCallingCheckers() {
        var checker = new RecordingChecker(true, true);
        var resolver = new DefaultFileReferencePermissionResolver(List.of(checker));

        assertFalse(resolver.canRead(" ", REFERENCE_ID, USER_ID));
        assertTrue(checker.invocations.isEmpty());
    }

    @Test
    void rejectsMissingReferenceIdWithoutCallingCheckers() {
        var checker = new RecordingChecker(true, true);
        var resolver = new DefaultFileReferencePermissionResolver(List.of(checker));

        assertFalse(resolver.canRead(REFERENCE_TYPE, null, USER_ID));
        assertTrue(checker.invocations.isEmpty());
    }

    @Test
    void rejectsMissingUserWithoutCallingCheckers() {
        var checker = new RecordingChecker(true, true);
        var resolver = new DefaultFileReferencePermissionResolver(List.of(checker));

        assertFalse(resolver.canRead(REFERENCE_TYPE, REFERENCE_ID, null));
        assertTrue(checker.invocations.isEmpty());
    }

    @Test
    void rejectsWhenNoCheckerSupportsReferenceType() {
        var checker = new RecordingChecker(false, true);
        var resolver = new DefaultFileReferencePermissionResolver(List.of(checker));

        assertFalse(resolver.canRead(REFERENCE_TYPE, REFERENCE_ID, USER_ID));
        assertEquals(List.of("supports"), checker.invocations);
    }

    @Test
    void rejectsWhenEverySupportingCheckerDenies() {
        var first = new RecordingChecker(true, false);
        var second = new RecordingChecker(true, false);
        var resolver = new DefaultFileReferencePermissionResolver(List.of(first, second));

        assertFalse(resolver.canRead(REFERENCE_TYPE, REFERENCE_ID, USER_ID));
        assertEquals(List.of("supports", "canRead"), first.invocations);
        assertEquals(List.of("supports", "canRead"), second.invocations);
    }

    @Test
    void allowsWhenASupportingCheckerAllows() {
        var unsupported = new RecordingChecker(false, true);
        var denied = new RecordingChecker(true, false);
        var allowed = new RecordingChecker(true, true);
        var resolver = new DefaultFileReferencePermissionResolver(List.of(unsupported, denied, allowed));

        assertTrue(resolver.canRead(REFERENCE_TYPE, REFERENCE_ID, USER_ID));
        assertEquals(List.of("supports"), unsupported.invocations);
        assertEquals(List.of("supports", "canRead"), denied.invocations);
        assertEquals(List.of("supports", "canRead"), allowed.invocations);
    }

    @Test
    void checkerFailureCannotBecomePermission() {
        var checker = new RecordingChecker(true, true);
        checker.failure = new IllegalStateException("permission source unavailable");
        var resolver = new DefaultFileReferencePermissionResolver(List.of(checker));

        assertFalse(resolver.canRead(REFERENCE_TYPE, REFERENCE_ID, USER_ID));
        assertEquals(List.of("supports", "canRead"), checker.invocations);
    }

    @Test
    void requireReadableUsesExistingUploadPermissionError() {
        var resolver = new DefaultFileReferencePermissionResolver(List.of(new RecordingChecker(true, false)));

        var exception = assertThrows(FileUploadException.class,
                () -> resolver.requireReadable(REFERENCE_TYPE, REFERENCE_ID, USER_ID));

        assertEquals(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, exception.getErrorCode());
    }

    private static final class RecordingChecker implements FileReferencePermissionChecker {

        private final boolean supported;

        private final boolean allowed;

        private final List<String> invocations = new ArrayList<>();

        private RuntimeException failure;

        private RecordingChecker(boolean supported, boolean allowed) {
            this.supported = supported;
            this.allowed = allowed;
        }

        @Override
        public boolean supports(String referenceType) {
            invocations.add("supports");
            return supported;
        }

        @Override
        public boolean canRead(String referenceType, UUID referenceId, UUID userId) {
            invocations.add("canRead");
            if (failure != null) {
                throw failure;
            }
            return allowed;
        }
    }
}
