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

package com.devops00.spectra.framework.assembler.converter;

/**
 * ID转换器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/4/20 11:41
 */
public interface IdConverter<ID> {

    /**
     * 将业务 ID 转换为可写入缓存键或传输数据的文本。
     *
     * @param id 要写入缓存键或响应数据的业务 ID；具体格式由 ID 类型实现决定。
     * @return 返回业务 ID 的稳定文本表示；输入是否允许为 null 及 null 的返回语义由具体实现定义。
     */
    String toString(ID id);

    /**
     * 将缓存键或传输数据中的文本恢复为业务 ID。
     *
     * @param value 从缓存键或请求数据读取的 ID 文本；具体解析规则由 ID 类型实现决定。
     * @return 返回从文本解析出的业务 ID；输入是否允许为 null 及解析失败时的异常语义由具体实现定义。
     */
    ID fromString(String value);
}
