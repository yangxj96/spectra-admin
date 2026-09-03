/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.mapper;

import org.apache.ibatis.annotations.Update;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies that external providers can confirm a part directly from PENDING. */
class FileUploadPartMapperStateContractTest {

    @Test
    void externalConfirmationMustTransitionPendingPartToConfirmed() throws NoSuchMethodException {
        var method = FileUploadPartMapper.class.getMethod("markExternalConfirmed", java.util.UUID.class, int.class,
                long.class, String.class, String.class);
        var update = method.getAnnotation(Update.class);

        assertThat(update).as("S3 confirmation must have its own state transition").isNotNull();
        assertThat(String.join(" ", update.value()))
                .contains("status = 'CONFIRMED'")
                .contains("AND status = 'PENDING'");
    }
}
