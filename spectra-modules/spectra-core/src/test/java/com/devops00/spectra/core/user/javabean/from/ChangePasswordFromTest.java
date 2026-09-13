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

package com.devops00.spectra.core.user.javabean.from;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证修改密码请求允许当前安全策略接受的字符，并继续执行长度上限校验。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class ChangePasswordFromTest {

    private static ValidatorFactory validatorFactory;

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void shouldAcceptUnicodePasswordWithAnySpecialCharacterAllowedBySystemPolicy() {
        ChangePasswordFrom request = new ChangePasswordFrom("old-password", "𐐀𐐨abc123-", "𐐀𐐨abc123-");

        assertFalse(validator.validateProperty(request, "newPassword").iterator().hasNext());
    }

    @Test
    void shouldRejectNewPasswordLongerThanTwentyCharacters() {
        ChangePasswordFrom request = new ChangePasswordFrom("old-password", "A".repeat(21), "A".repeat(21));

        assertTrue(validator.validateProperty(request, "newPassword")
                .stream()
                .anyMatch(violation -> violation.getMessage().equals("密码长度不能超过 20 位")));
    }
}
