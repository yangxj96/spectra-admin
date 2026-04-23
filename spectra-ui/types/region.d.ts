export {};

declare global {
    // 区域路径
    interface RegionPathVO {
        ids: string[];
        names: string[];
        full_name: string;
    }

    // 行政区域信息
    type Region = BaseEntity & {
        // 区域名称
        name: string;

        // 区域全称，如 北京市/北京市/东城区
        full_name: string;

        // 简称
        short_name: string;

        // 区域编码
        code: string;

        // 区域路径，如 /110000/110100/110101
        path: string;

        // 上级ID
        pid: string;

        // 行政区划层级:1省 2地级市 3县级 4乡级 5村级
        level: string;

        // 状态：true-启用 false-停用
        status: boolean;

        // 排序
        sort: number;
    };
}
