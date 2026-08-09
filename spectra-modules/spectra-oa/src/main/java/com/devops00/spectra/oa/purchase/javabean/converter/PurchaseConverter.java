/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.oa.purchase.javabean.converter;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.purchase.javabean.entity.Purchase;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseItem;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceipt;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceiptItem;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseItemFrom;
import com.devops00.spectra.oa.purchase.javabean.from.PurchaseSaveFrom;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseItemVO;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseReceiptItemVO;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseReceiptVO;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseVO;

/**
 * 采购 MapStruct 转换器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface PurchaseConverter {
    /**
     * 采购申请实体转视图对象。
     */
    PurchaseVO toVO(Purchase source);

    /**
     * 采购保存入参转实体。
     */
    Purchase toEntity(PurchaseSaveFrom source);

    /**
     * 使用保存入参更新采购实体。
     */
    void updateEntity(PurchaseSaveFrom source, @MappingTarget Purchase target);

    /**
     * 采购明细入参转实体。
     */
    PurchaseItem toItemEntity(PurchaseItemFrom source);

    /**
     * 采购明细实体转视图对象。
     */
    PurchaseItemVO toItemVO(PurchaseItem source);

    /**
     * 采购收货单实体转视图对象。
     */
    PurchaseReceiptVO toReceiptVO(PurchaseReceipt source);

    /**
     * 采购收货明细实体转视图对象。
     */
    PurchaseReceiptItemVO toReceiptItemVO(PurchaseReceiptItem source);
}
