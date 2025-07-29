package com.yangxj96.spectra.core.system.javabean.mapstruct;

import com.yangxj96.spectra.core.system.javabean.entity.Organization;
import com.yangxj96.spectra.core.system.javabean.from.OrganizationFrom;
import com.yangxj96.spectra.core.system.javabean.vo.OrganizationTreeVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 组织机构的数据转换使用
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/14
 */
@Mapper(componentModel = "spring")
public interface OrganizationMapstruct {

    /**
     * 实体转树形
     *
     * @param organizations 实体列表
     * @return 树形列表
     */
    List<OrganizationTreeVo> toTreeVOS(List<Organization> organizations);

    /**
     * 实体转树形
     *
     * @param organization 实体
     * @return 树形
     */
    @Mapping(target = "children", ignore = true)
    OrganizationTreeVo toTreeVO(Organization organization);

    /**
     * 入参转实体
     *
     * @param from 入参
     * @return 实体
     */
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Organization toEntity(OrganizationFrom from);

}

