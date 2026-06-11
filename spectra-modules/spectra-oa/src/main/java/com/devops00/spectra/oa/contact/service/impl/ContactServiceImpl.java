package com.devops00.spectra.oa.contact.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.contact.javabean.entity.Contact;
import com.devops00.spectra.oa.contact.mapper.ContactMapper;
import com.devops00.spectra.oa.contact.service.ContactService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 通讯录主表-服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 11:56
@Slf4j
@Service
public class ContactServiceImpl extends BaseServiceImpl<ContactMapper, Contact> implements ContactService {
}
