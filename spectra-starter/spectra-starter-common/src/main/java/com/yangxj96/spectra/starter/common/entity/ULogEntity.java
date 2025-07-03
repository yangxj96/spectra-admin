package com.yangxj96.spectra.starter.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 切面中存储的数据的实体
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ULogEntity implements Serializable {

    /**
     * 操作说明
     */
    private String explain;

    /**
     * 请求参数
     */
    private String args;

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 请求URL
     */
    private String url;

    /**
     * 响应状态
     */
    private Short status;

    /**
     * 响应内容
     */
    private String result;

    /**
     * 耗时
     */
    private Long timeCost;

    /**
     * 当前用户的TOKEN
     */
    private String token;
}
