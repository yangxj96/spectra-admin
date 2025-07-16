package com.yangxj96.spectra.service.core.javabean.mapstruct;

import com.yangxj96.spectra.service.core.javabean.entity.Organization;
import com.yangxj96.spectra.service.core.javabean.from.OrganizationFrom;
import com.yangxj96.spectra.service.core.javabean.vo.OrganizationTreeVo;
import com.yangxj96.spectra.share.javabean.OrganizationShareDTO;
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
    Organization toEntity(OrganizationFrom from);


    /**
     * 转换成组织机构共享DTO对象
     *
     * @param entity 组织机构entity
     * @return 共享DTO对象
     */
    OrganizationShareDTO toShareDTO(Organization entity);

    /**
     * 转换成组织机构共享DTO对象列表
     *
     * @param entities 组织机构entity列表
     * @return 共享DTO对象列表
     */
    List<OrganizationShareDTO> toShareDTOs(List<Organization> entities);

}

