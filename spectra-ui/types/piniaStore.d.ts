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

// useAppStore()的state
type StoreApp = {
    // 全局i18n语言
    lang: object;
    // 菜单列表
    menus: Menu[];
    // 是否展开菜单
    unfold: boolean;
};

// usePropsStore()的state
type StoreProps = {
    // 用户详情
    personal_details: boolean;
    // 修改密码
    change_password: boolean;
};

// useTabsStore()的state
type StoreTabs = {
    // 选项卡存储的数组,不包含首页这个固定项
    tabs: string[];
    // 当前激活的选项卡
    active: string | undefined;
};

// useUserStore()的state
type StoreUser = {
    // 用户登录的token
    token: Token;
};

// useDictStore()的state
type StoreDict = {
    // 字典缓存数组
    dicts: Record<string, DictData[]>;
};
