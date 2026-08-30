/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.javabean.from;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class FileReferenceRequest {

    @NotNull
    private UUID fileAssetId;

    @NotBlank
    @Size(max = 80)
    private String referenceType;

    @NotNull
    private UUID referenceId;

    @NotBlank
    @Size(max = 80)
    private String purpose;

    @Size(max = 255)
    private String displayName;
}
