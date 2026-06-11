package com.devops00.spectra.oa.meeting.javabean.converter;


import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.meeting.javabean.entity.Meeting;
import com.devops00.spectra.oa.meeting.javabean.from.MeetingCreateFrom;
import org.mapstruct.Mapper;

/// 会议javabean转换器
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/3/30 15:29
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface MeetingConverter {

    /// 入参转实体
    Meeting toEntity(MeetingCreateFrom source);

}
