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

package com.devops00.spectra.oa.contract.service.job;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.devops00.spectra.oa.contract.service.ContractService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 合同履约节点提醒任务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ContractReminderJob {

    private final ContractService contractService;

    @Scheduled(cron = "0 0 1 * * *")
    public void sendDueMilestoneReminders() {
        var sent = contractService.sendDueMilestoneReminders();
        if (sent > 0) {
            log.info("合同履约节点提醒发送完成: count={}", sent);
        }
    }
}
