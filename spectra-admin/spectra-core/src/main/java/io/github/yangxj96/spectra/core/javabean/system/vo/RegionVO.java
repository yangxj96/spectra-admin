package io.github.yangxj96.spectra.core.javabean.system.vo;


import io.github.yangxj96.spectra.common.constant.RegionLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 行政区域响应VO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/1/30 15:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegionVO implements Serializable {

    /// 区域名称
    private String name;

    /// 区域全称，如 北京市/北京市/东城区
    private String fullName;

    /// 简称
    private String shortName;

    /// 区域编码
    private String code;

    /// 区域路径，如 /110000/110100/110101
    private String path;

    /// 上级ID
    private String pid;

    /// 行政区划层级:1省 2地级市 3县级 4乡级 5村级
    private RegionLevel level;

    /// 状态：true-启用 false-停用
    private Boolean status;

    /// 排序
    private Integer sort;

}
