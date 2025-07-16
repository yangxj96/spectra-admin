package com.yangxj96.spectra.share.service;

import com.yangxj96.spectra.share.javabean.OrganizationShareDTO;

import java.util.List;

/**
 * 组织机构共享service
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
public interface ShareOrganizationService {

    /**
     * 获取所有有权用的组织机构
     * @return 共享组织机构列表
     */
    List<OrganizationShareDTO> all();

}
