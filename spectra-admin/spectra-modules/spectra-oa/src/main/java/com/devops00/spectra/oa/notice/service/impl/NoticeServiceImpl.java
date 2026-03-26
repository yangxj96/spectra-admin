package com.devops00.spectra.oa.notice.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.oa.notice.javabean.entity.Notice;
import com.devops00.spectra.oa.notice.mapper.NoticeMapper;
import com.devops00.spectra.oa.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 公告表-主表Service默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/26 16:41
@Slf4j
@Service
public class NoticeServiceImpl extends BaseServiceImpl<NoticeMapper, Notice> implements NoticeService {
}
