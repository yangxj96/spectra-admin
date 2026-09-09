/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.contact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.oa.contact.javabean.vo.ContactVO;

/**
 * 基于系统用户与部门的 OA 通讯录服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public interface ContactService {

    /**
     * 分页查询启用用户的公开联系信息。
     *
     * @param page    分页条件，包含页码、页大小和排序字段。
     * @param keyword 联系人姓名、用户名、部门名称或联系方式的模糊查询关键字；为空时查询全部启用用户。
     * @return 返回按分页条件查询的OA 通讯录联系人分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<ContactVO> page(PageFrom page, String keyword);
}
