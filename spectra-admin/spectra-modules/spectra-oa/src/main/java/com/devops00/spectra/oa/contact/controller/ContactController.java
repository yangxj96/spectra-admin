package com.devops00.spectra.oa.contact.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.configure.ulog.annotation.ULog;
import com.devops00.spectra.oa.contact.javabean.entity.Contact;
import com.devops00.spectra.oa.contact.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/// 通讯录主接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/5 23:22
@RestController
@RequestMapping("/oa/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService bindService;

    @ULog("分页查通讯录")
    @GetMapping("/page")
    public IPage<Contact> page(PageFrom page) {
        return bindService.page(page.toPage());
    }

}
