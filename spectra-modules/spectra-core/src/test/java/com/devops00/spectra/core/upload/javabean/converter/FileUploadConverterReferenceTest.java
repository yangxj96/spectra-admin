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

package com.devops00.spectra.core.upload.javabean.converter;

import com.devops00.spectra.core.upload.javabean.entity.FileReference;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证文件引用行主键与业务引用 ID 的独立映射及 snake_case 序列化。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class FileUploadConverterReferenceTest {

    @Test
    void referenceRowIdAndBusinessIdStayIndependentInSnakeCase() throws Exception {
        UUID rowId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        var reference = new FileReference();
        reference.setId(rowId);
        reference.setReferenceId(businessId);
        reference.setReferenceType("OA_LEAVE");
        reference.setFileAssetId(UUID.randomUUID());
        reference.setPurpose("CONTENT");
        reference.setDisplayName("synthetic.txt");

        var vo = new FileUploadConverterImpl(null).toReferenceVO(reference);
        assertEquals(rowId, vo.getReferenceId());
        assertEquals(businessId, vo.getBusinessReferenceId());

        var mapper = JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE).build();
        String json = mapper.writeValueAsString(vo);
        assertTrue(json.contains("\"reference_id\":\"" + rowId + "\""), json);
        assertTrue(json.contains("\"business_reference_id\":\"" + businessId + "\""), json);
    }
}
