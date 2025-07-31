package com.yangxj96.spectra.common.base;

/**
 * 范围实体的要求,
 * 这个实体有组织机构ID这个字段,则查询的时候需要进行权限范围的控制
 */
public interface ScopeEntity {

    /**
     * 获取组织机构ID
     *
     * @return 组织机构ID
     */
    Long getOrganizationId();

    /**
     * 设置组织机构ID
     *
     * @param oid 组织机构ID
     */
    void setOrganizationId(Long oid);


}
