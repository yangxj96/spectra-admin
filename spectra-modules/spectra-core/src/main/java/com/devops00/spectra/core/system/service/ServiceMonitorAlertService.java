/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorSample;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorAlertRuleFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertEventVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertRuleVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorAlertSummaryVO;

import java.util.List;
import java.util.UUID;

/** 服务监控告警规则与事件服务。 */
public interface ServiceMonitorAlertService {

    /** 根据最新采样评估规则并推进事件状态。 */
    void evaluate(ServiceMonitorSample sample);

    /** 查询规则。 */
    List<ServiceMonitorAlertRuleVO> listRules();

    /** 修改规则。 */
    void modifyRule(UUID id, ServiceMonitorAlertRuleFrom from);

    /** 查询告警事件。 */
    List<ServiceMonitorAlertEventVO> listEvents(boolean activeOnly);

    /** 查询告警摘要。 */
    ServiceMonitorAlertSummaryVO getSummary();
}
