package com.devops00.spectra.core.notification.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.core.notification.javabean.entity.Notification;
import com.devops00.spectra.core.notification.javabean.vo.NotificationVO;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 消息mapstruct
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface NotificationConverter {

    /**
     * 实体转VO
     */
    NotificationVO toVO(Notification source);

    /**
     * 分页转换
     */
    @Mapping(target = "pages", ignore = true)
    Page<NotificationVO> toVOPage(Page<Notification> source);
}
