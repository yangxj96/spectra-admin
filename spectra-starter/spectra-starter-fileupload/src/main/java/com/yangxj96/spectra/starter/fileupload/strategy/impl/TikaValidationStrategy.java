/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.starter.fileupload.strategy.impl;

import com.yangxj96.spectra.starter.fileupload.strategy.FileTypeValidationStrategy;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public class TikaValidationStrategy implements FileTypeValidationStrategy {

    private final List<String> allowedMimes;

    private final Tika tika = new Tika();

    public TikaValidationStrategy(List<String> allowedMimes) {
        this.allowedMimes = allowedMimes;
    }

    @Override
    public boolean isValid(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String detectedMimeType = tika.detect(file.getInputStream(), file.getOriginalFilename());
        return allowedMimes.stream().anyMatch(mime -> mime.equalsIgnoreCase(detectedMimeType));
    }
}
