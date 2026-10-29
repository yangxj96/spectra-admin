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

package com.devops00.spectra.launch.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 使用 Java 参数文件启动第二个应用进程，避开 Windows 命令行长度上限。
 */
final class SecondProcessStarter {

    private SecondProcessStarter() {
    }

    static Process start(Path log, Class<?> mainClass, List<String> applicationArgs,
                         Map<String, String> environment)
            throws IOException {
        Path argFile = Files.createTempFile(log.toAbsolutePath().getParent(), "second-process-", ".args");
        List<String> lines = new ArrayList<>();
        lines.add("-Duser.timezone=UTC");
        lines.add("-cp");
        lines.add(System.getProperty("java.class.path"));
        lines.add(mainClass.getName());
        lines.addAll(applicationArgs);
        Files.write(argFile, lines, StandardCharsets.UTF_8);

        String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
        ProcessBuilder builder = new ProcessBuilder(java, "@" + argFile.toAbsolutePath())
                .redirectErrorStream(true)
                .redirectOutput(log.toFile());
        builder.environment().putAll(environment);
        return builder.start();
    }
}
