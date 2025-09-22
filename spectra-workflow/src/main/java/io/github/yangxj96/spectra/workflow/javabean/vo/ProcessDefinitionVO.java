package io.github.yangxj96.spectra.workflow.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * 流程定义响应VO
 */
@Data
@Builder
public class ProcessDefinitionVO implements Serializable {

    /**
     * 流程ID
     */
    private String id;

    /**
     * 流程key
     */
    private String key;

    /**
     * 流程名称
     */
    private String name;

    /**
     * 流程版本
     */
    private Integer version;

    /**
     * 部署ID
     */
    private String deploymentId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 是否挂起
     */
    private Boolean suspended;
}
