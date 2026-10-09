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

package com.devops00.spectra.framework.persistence.pagination;

import com.devops00.spectra.framework.web.advice.exception.CommonExceptionAdvice;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 验证分页参数绑定与正式异常处理器的 HTTP 边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/08
 */
class PageFromSortHttpTest {
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new SortController())
            .setControllerAdvice(new CommonExceptionAdvice())
            .build();

    @ParameterizedTest
    @ValueSource(strings = {"password", "id desc", "(select 1)", "id;select 1", "other.id", "id--", "i d"})
    void maliciousSortReturnsSafeBadRequest(String field) throws Exception {
        mvc.perform(get("/sort").param("orders[0].column", field).param("orders[0].asc", "true"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("不支持的分页排序字段")))
                .andExpect(content().string(not(containsString(field))));
    }

    @Test
    void validAndAbsentSortAreAccepted() throws Exception {
        mvc.perform(get("/sort").param("orders[0].column", "id").param("orders[0].asc", "false"))
                .andExpect(status().isOk())
                .andExpect(content().string("d.id:false"));
        mvc.perform(get("/sort")).andExpect(status().isOk()).andExpect(content().string("default"));
    }

    @Test
    void fixedSortRejectsEvenKnownField() throws Exception {
        mvc.perform(get("/fixed-sort").param("orders[0].column", "id"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/fixed-sort")).andExpect(status().isOk());
    }

    @RestController
    static class SortController {
        @GetMapping("/sort")
        String sort(PageFrom request) {
            var page = request.toPage(Map.of("id", "d.id"));
            return page.orders().isEmpty()
                    ? "default"
                    : page.orders().getFirst().getColumn() + ":" + page.orders().getFirst().isAsc();
        }

        @GetMapping("/fixed-sort")
        String fixed(PageFrom request) {
            request.requireUnsorted();
            return "default";
        }
    }
}
