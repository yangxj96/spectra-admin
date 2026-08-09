/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.oa.contact.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
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
     */
    IPage<ContactVO> page(PageFrom page, String keyword);
}
