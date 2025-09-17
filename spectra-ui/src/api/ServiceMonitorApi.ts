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

import http from "@/plugin/request/index.ts";

export default {
    // 获取CPU信息
    async getCPUInfo() {
        return await http.get<IResult<CPUInfo>>("/api/service/monitor/getCPUInfo").then(res => res.data);
    },

    // 获取内存信息
    async getRAMInfo() {
        return await http.get<IResult<RAMInfo>>("/api/service/monitor/getRAMInfo").then(res => res.data);
    },

    // 获取JVM信息
    async getJVMInfo() {
        return await http.get<IResult<JVMInfo>>("/api/service/monitor/getJVMInfo").then(res => res.data);
    }
};
