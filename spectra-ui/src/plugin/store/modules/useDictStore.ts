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

import DictApi from "@/api/DictApi";
import { defineStore } from "pinia";
import PQueue from "p-queue";

// 创建一个串行队列（concurrency=1）
const serialQueue = new PQueue({ concurrency: 1 });

const useDictStore = defineStore("dict", {
    state: (): StoreDict => ({
        dicts: {}
    }),
    actions: {
        async getDictData(key: string): Promise<void | DictData[]> {
            return serialQueue.add(async () => {
                if (this.dicts[key]) {
                    return this.dicts[key];
                }
                try {
                    const { code, data } = await DictApi.getDataByTypeCode(key);
                    if (code === 200) {
                        this.dicts[key] = data ?? [];
                        return this.dicts[key];
                    } else {
                        console.log(`获取字典失败,${key}`);
                        return [];
                    }
                } catch (error) {
                    console.log("发生异常", error);
                    return [];
                }
            });
        }
    }
});

export default useDictStore;
