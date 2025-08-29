package io.github.yangxj96.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

/**
 * CPU信息响应实体
 */
@Data
@Builder
public class CPUInfoVO {

    /**
     * CPU名称
     */
    private String name;

    /**
     * CPU 制造商
     */
    private String vendor;

    /**
     * 家族编号
     */
    private String family;

    /**
     * 型号编号
     */
    private String model;

    /**
     * 步进(修订版本)
     */
    private String stepping;

    /**
     * 完整标识字符串
     */
    private String identifier;

    /**
     * 是否64位
     */
    private Boolean is64bit;

    /**
     * 物理核心数量
     */
    private Integer physicalCores;

    /**
     * 逻辑核心数（支持超线程）
     */
    private Integer logicalCores;

    /**
     * 最大支持频率
     */
    private Long maxFrequencyHz;

    /**
     * 最大支持频率
     */
    private String maxFrequencyGhz;

}
