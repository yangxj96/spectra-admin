package com.devops00.spectra.core.runner;


import com.devops00.spectra.common.constant.LogPrefix;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.InputStream;

/// 字体注册Runner
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/1/14 14:10
@Slf4j
@Component
public class FontRegisterRunner implements ApplicationRunner {

    @Override
    public void run(@Nullable ApplicationArguments args) {
        try (InputStream in =
                     FontRegisterRunner.class
                             .getResourceAsStream("/fonts/Inter-Regular.ttf")) {

            if (in == null) {
                throw new IllegalStateException("Font not found: /fonts/Inter-Regular.ttf");
            }

            Font font = Font.createFont(Font.TRUETYPE_FONT, in);

            GraphicsEnvironment ge =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();

            boolean success = ge.registerFont(font);

            log.debug("{}注册字体{}=>{}", LogPrefix.CORE.p(), font.getFontName(), success);

        } catch (Exception e) {
            throw new IllegalStateException("Font register failed", e);
        }
    }

}
