/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.configure.datascope;

/**
 * 数据范围上下文工具类
 */
public class DataScopeContext {

    private DataScopeContext() {
    }

    private static final ThreadLocal<DataScopeInfo> DATA_SCOPE_HOLDER = new ThreadLocal<>();

    public static void set(DataScopeInfo info) {
        DATA_SCOPE_HOLDER.set(info);
    }

    public static DataScopeInfo get() {
        return DATA_SCOPE_HOLDER.get();
    }

    public static void clear() {
        DATA_SCOPE_HOLDER.remove();
    }

}