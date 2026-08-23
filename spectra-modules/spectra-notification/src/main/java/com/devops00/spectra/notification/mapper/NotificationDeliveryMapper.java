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

package com.devops00.spectra.notification.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.vo.NotificationOverviewErrorVO;
import com.devops00.spectra.notification.javabean.vo.NotificationOverviewTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.Instant;
import java.util.List;

/**
 * 通知投递 Mapper。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Mapper
public interface NotificationDeliveryMapper extends BaseMapper<NotificationDeliveryEntity> {

    /**
     * 查询时间窗口内按小时聚合的投递趋势；聚合 SQL 不返回正文、地址或供应商响应。
     */
    @InterceptorIgnore(dataPermission = "true")
    List<NotificationOverviewTrendVO> selectOverviewTrend(@Param("from") Instant from,
                                                          @Param("to") Instant to);

    /**
     * 查询时间窗口内最近的脱敏投递错误。
     */
    @InterceptorIgnore(dataPermission = "true")
    List<NotificationOverviewErrorVO> selectRecentErrors(@Param("from") Instant from,
                                                         @Param("to") Instant to,
                                                         @Param("limit") int limit);
}
