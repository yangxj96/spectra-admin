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

package com.devops00.spectra.core.common.runner;

import com.devops00.spectra.common.constant.LogPrefix;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

/**
 * 字体注册Runner
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/1/14 14:10
 */
@Slf4j
@Component
public class FontRegisterRunner implements ApplicationRunner {

    @Override
    public void run(@Nullable ApplicationArguments args) {
        try (InputStream in = FontRegisterRunner.class.getResourceAsStream("/fonts/Inter-Regular.ttf")) {

            if (in == null) {
                throw new IllegalStateException("Font not found: /fonts/Inter-Regular.ttf");
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, in);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            boolean success = ge.registerFont(font);

            log.debug("{}注册字体{}=>{}", LogPrefix.CORE.p(), font.getFontName(), success);
        } catch (Exception e) {
            throw new IllegalStateException("Font register failed", e);
        }
    }
}
