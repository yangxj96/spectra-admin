/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.core.system.javabean.converter.DictGroupConverter;
import com.devops00.spectra.core.system.javabean.converter.DictItemConverter;
import com.devops00.spectra.core.system.javabean.entity.DictGroup;
import com.devops00.spectra.core.system.javabean.from.DictGroupFrom;
import com.devops00.spectra.core.system.service.DictGroupService;
import com.devops00.spectra.core.system.service.DictItemService;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DictGroupModifyTest {

    private final DictGroupConverter groupConverter = mock(DictGroupConverter.class);
    private final DictItemConverter itemConverter = mock(DictItemConverter.class);
    private final DictGroupService groupService = mock(DictGroupService.class);
    private final DictItemService itemService = mock(DictItemService.class);

    @Test
    void missingGroupReturnsNotFoundInsteadOfNullPointer() {
        var from = from();
        when(groupService.getById(from.getId())).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service().modifyGroup(from));

        verifyNoInteractions(groupConverter);
    }

    @Test
    void builtinGroupIsRejectedBeforeVersionAndMapping() {
        var from = from();
        var group = new DictGroup();
        group.setId(from.getId());
        group.setBuiltin(true);
        when(groupService.getById(from.getId())).thenReturn(group);

        assertThrows(BuiltinDataException.class, () -> service().modifyGroup(from));

        verifyNoInteractions(groupConverter);
    }

    @Test
    void staleVersionIsRejectedBeforeUpdate() {
        var from = from();
        var group = new DictGroup();
        group.setId(from.getId());
        group.setBuiltin(false);
        group.setVersion(2L);
        when(groupService.getById(from.getId())).thenReturn(group);

        assertThrows(EntityUpdateException.class, () -> service().modifyGroup(from));

        verifyNoInteractions(groupConverter);
    }

    @Test
    void editableGroupWithCurrentVersionIsUpdated() {
        var from = from();
        var group = new DictGroup();
        group.setId(from.getId());
        group.setBuiltin(false);
        group.setVersion(from.getVersion());
        when(groupService.getById(from.getId())).thenReturn(group);
        when(groupConverter.toEntity(from)).thenReturn(group);
        when(groupService.updateById(group)).thenReturn(true);

        service().modifyGroup(from);

        verify(groupService).updateById(any(DictGroup.class));
    }

    private DictServiceImpl service() {
        return new DictServiceImpl(groupConverter, itemConverter, groupService, itemService);
    }

    private static DictGroupFrom from() {
        var from = new DictGroupFrom();
        from.setId(UUID.randomUUID());
        from.setVersion(1L);
        from.setName("测试字典");
        from.setCode("TEST_DICT");
        from.setState(true);
        return from;
    }
}
