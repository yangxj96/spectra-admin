import PQueue from "p-queue";
import { defineStore } from "pinia";

import { dictApi } from "@/api/system/dict.ts";

// 创建一个串行队列（concurrency=1）
const serialQueue = new PQueue({ concurrency: 1 });

export const useDictStore = defineStore("dict", {
    state: (): StoreDict => ({
        dicts: {}
    }),
    actions: {
        /**
         * 获取字典数据
         * @param key 字典组KEY
         */
        async getDictData(key: string): Promise<void | DictItem[]> {
            return serialQueue.add(async () => {
                if (this.dicts[key]) {
                    return this.dicts[key];
                }
                try {
                    const data = await dictApi.getDataByTypeCode(key);
                    this.dicts[key] = data ?? [];
                    return this.dicts[key];
                } catch (error) {
                    console.log("发生异常", error);
                    return [];
                }
            });
        },
        /**
         * 根据字典组 key 和字典项 value 返回字典项对象 <br/>
         * 会自动尝试加载字典 <br/>
         * @param key 字典组的KEY
         * @param value 字典项的VALUE
         */
        async getDictItem(key: string, value: string): Promise<DictItem | undefined> {
            let dictItems = this.dicts[key];

            // 如果没有缓存，先尝试加载
            dictItems ??= (await this.getDictData(key)) ?? [];

            // 找不到就返回 undefined
            return dictItems.find(item => item.value === value);
        },
        /**
         * 根据字典组 key 和字典项 value 返回字典项对象 <br/>
         * 不会自动尝试加载字典 <br/>
         * @param key 字典组的KEY
         * @param value 字典项的VALUE
         */
        getDictItemSync(key: string, value: string | number): DictItem | undefined {
            return this.dicts[key]?.find(item => item.value.toString() === value.toString());
        }
    }
});
