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

package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.exception.DataException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户导入关联部门编码的统一解析与校验规则。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/27
 */
final class UserImportDepartmentCodes {

    private UserImportDepartmentCodes() {
    }

    /** 规范化每个编码的空格，但保留空分段供 Preview 显式报错。 */
    static String normalize(String source) {
        if (source == null) {
            return "";
        }
        var values = source.split(";", -1);
        for (int index = 0; index < values.length; index++) {
            values[index] = values[index].trim();
        }
        return String.join(";", values);
    }

    /** 解析编码并返回所有可定位到该行的校验错误。 */
    static ParseResult parse(String source, String primaryCode, Map<String, UUID> departmentIds) {
        if (source == null || source.isBlank()) {
            return new ParseResult(List.of(), List.of());
        }

        var codes = new ArrayList<String>();
        var errors = new LinkedHashSet<String>();
        var seen = new HashSet<String>();
        for (var segment : source.split(";", -1)) {
            var code = segment.trim();
            if (code.isEmpty()) {
                errors.add("关联部门编码不能包含空项");
                continue;
            }
            if (!seen.add(code)) {
                errors.add("关联部门编码不能重复");
                continue;
            }
            codes.add(code);
            if (code.equals(primaryCode)) {
                errors.add("主部门不能重复作为关联部门");
            } else if (!departmentIds.containsKey(code)) {
                errors.add("关联部门编码不存在");
            }
        }
        return new ParseResult(List.copyOf(codes), List.copyOf(errors));
    }

    /** 将 Preview 已确认的编码解析成部门 ID；Apply 时继续拒绝无效快照。 */
    static List<UUID> resolveIds(String source, String primaryCode, Map<String, UUID> departmentIds) {
        var result = parse(source, primaryCode, departmentIds);
        if (!result.errors().isEmpty()) {
            throw new DataException(result.errors().getFirst());
        }
        return result.codes().stream().map(departmentIds::get).toList();
    }

    /** 解析后的关联编码和行级错误。 */
    record ParseResult(List<String> codes, List<String> errors) {
    }
}
