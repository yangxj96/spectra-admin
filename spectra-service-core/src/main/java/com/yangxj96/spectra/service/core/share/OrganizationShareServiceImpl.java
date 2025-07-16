package com.yangxj96.spectra.service.core.share;

import com.yangxj96.spectra.service.core.javabean.entity.Organization;
import com.yangxj96.spectra.service.core.javabean.mapstruct.OrganizationMapstruct;
import com.yangxj96.spectra.service.core.service.OrganizationService;
import com.yangxj96.spectra.share.OrganizationShareService;
import com.yangxj96.spectra.share.javabean.OrganizationShareDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组织机构共享service
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/16
 */
@Service
public class OrganizationShareServiceImpl implements OrganizationShareService {

    @Resource
    private OrganizationService organizationService;

    @Resource
    private OrganizationMapstruct organizationMapstruct;

    @Override
    public List<OrganizationShareDTO> all() {
        List<Organization> organizations = organizationService.list();
        return organizationMapstruct.toShareDTOs(organizations);
    }

}
