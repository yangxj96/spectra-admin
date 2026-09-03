/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.oa.support;

import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationAttachment;
import com.devops00.spectra.oa.application.mapper.ApplicationAttachmentMapper;
import com.devops00.spectra.oa.application.mapper.ApplicationMapper;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.javabean.entity.ContractVersion;
import com.devops00.spectra.oa.contract.mapper.ContractMapper;
import com.devops00.spectra.oa.contract.mapper.ContractVersionMapper;
import com.devops00.spectra.oa.document.javabean.entity.Document;
import com.devops00.spectra.oa.document.javabean.entity.DocumentVersion;
import com.devops00.spectra.oa.document.mapper.DocumentMapper;
import com.devops00.spectra.oa.document.mapper.DocumentVersionMapper;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.file.FileReferencePermissionChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OaFileReferencePermissionChecker implements FileReferencePermissionChecker {

    private static final Set<String> TYPES = Set.of("OA_APPLICATION_ATTACHMENT", "OA_CONTRACT_VERSION", "OA_DOCUMENT_VERSION",
            "OA_REIMBURSEMENT_ATTACHMENT");

    private final ContractVersionMapper contractVersionMapper;
    private final ContractMapper contractMapper;
    private final DocumentVersionMapper documentVersionMapper;
    private final DocumentMapper documentMapper;
    private final ApplicationAttachmentMapper attachmentMapper;
    private final ApplicationMapper applicationMapper;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public boolean supports(String referenceType) {
        return TYPES.contains(referenceType);
    }

    @Override
    public boolean canRead(String referenceType, UUID referenceId, UUID userId) {
        if (userId == null || !userId.equals(securityContextAccessor.currentUserId()))
            return false;
        return switch (referenceType) {
            case "OA_APPLICATION_ATTACHMENT" -> canReadApplication(attachmentMapper.selectById(referenceId), userId);
            case "OA_CONTRACT_VERSION" -> canReadContract(contractVersionMapper.selectById(referenceId), userId);
            case "OA_DOCUMENT_VERSION" -> canReadDocument(documentVersionMapper.selectById(referenceId), userId);
            case "OA_REIMBURSEMENT_ATTACHMENT" -> canReadApplication(attachmentMapper.selectById(referenceId), userId);
            default -> false;
        };
    }

    private boolean canReadContract(ContractVersion version, UUID userId) {
        if (version == null || version.getDeleted() != null)
            return false;
        Contract contract = contractMapper.selectById(version.getContractId());
        return contract != null && visible(contract.getOwnerId(), contract.getDepartmentId(), contract.getVisibility(), userId);
    }

    private boolean canReadDocument(DocumentVersion version, UUID userId) {
        if (version == null || version.getDeleted() != null)
            return false;
        Document document = documentMapper.selectById(version.getDocumentId());
        return document != null && visible(document.getOwnerId(), document.getDepartmentId(), document.getVisibility(), userId);
    }

    private boolean canReadApplication(ApplicationAttachment attachment, UUID userId) {
        if (attachment == null || attachment.getDeleted() != null)
            return false;
        Application application = applicationMapper.selectById(attachment.getApplicationId());
        return application != null
                && (Objects.equals(application.getApplicantId(), userId)
                        || Objects.equals(application.getDepartmentId(), currentDepartmentId()));
    }

    private boolean visible(UUID ownerId, UUID departmentId, String visibility, UUID userId) {
        if (Objects.equals(ownerId, userId) || "PUBLIC".equals(visibility))
            return true;
        return "DEPARTMENT".equals(visibility) && Objects.equals(departmentId, currentDepartmentId());
    }

    private UUID currentDepartmentId() {
        var user = securityContextAccessor.currentUser();
        return user == null ? null : user.getDepartmentId();
    }
}
