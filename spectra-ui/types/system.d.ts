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

// 菜单
type Menu = BaseEntity & {
    //父级ID
    pid: string;
    //图标
    icon: string;
    //名称
    name: string;
    //路径
    path: string;
    //组件地址
    component: string;
    //排序
    sort: number;
    //布局
    layout?: string;
    //模块
    module?: string;
    //参数
    params?: never;
    //元数据
    meta?: never;
    //子级
    children?: Menu[];
};

// 部门
type Organization = BaseEntity & {
    // 上级ID
    pid: string;
    // 名称
    name: string;
    // 代码
    code: string;
    // 类型
    type: number;
    // 地址
    address: string;
    // 负责人ID
    manager_id: string;
    // 备注
    remark?: string;
};

// 组织机构树形
type OrganizationTree = Organization & {
    // 子级
    children?: OrganizationTree[];
};

// 字典组
type DictGroup = BaseEntity & {
    //父级ID
    pid: string;
    //字典类型名称
    name: string;
    //字典类型编码
    code: string;
    //状态
    state: number;
    //备注
    remark?: string;
    //是否内置
    builtin?: boolean;
    //是否隐藏
    hide?: boolean;
};

// 字典组树形结构
type DictTypeTree = DictGroup & {
    // 下级内容
    children?: DictTypeTree[];
};

// 字典数据
type DictData = BaseEntity & {
    //字典组ID
    gid: string;
    //字典标签
    label: string;
    //字典值
    value: string;
    //排序
    sort: number;
    //状态
    state: number;
    //备注
    remark?: string;
};

// CPU信息
type CPUInfo = {
    name: string;
    load: string;
    vendor: string;
    family: string;
    model: string;
    stepping: string;
    identifier: string;
    is64bit: boolean;
    physical_cores: number;
    logical_cores: number;
    max_frequency_hz: string;
    max_frequency_ghz: string;
};

// CPU信息
type RAMInfo = {
    summary: string;
    count: string;
    total_capacity_bytes: string;
    total_capacity_gb: string;
    slots: RAMSlotInfo[];
};

// CPU信息(单条)
type RAMSlotInfo = {
    slot: number;
    memory_type: string;
    clock_speed_hz: string;
    clock_speed_mhz: string;
    capacity_bytes: string;
    capacity_gb: string;
};

// JVM信息
type JVMInfo = {
    jvm_name: string;
    jvm_vendor: string;
    jvm_version: string;
    jvm_spec_name;
    jvm_spec_version: string;
    jvm_spec_vendor: string;
    java_version: string;
    java_home: string;
    java_vendor: string;
    java_vendor_url: string;
    start_time: string;
    pid: string;
    process_id: string;
    jvm_arguments: string[];
    system_properties: Map<string, string>;
    class_path: string;
    library_path: string;
};
