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

package com.devops00.spectra.common.port.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

/** 仅支持 Cron 和 Simple 两种 Quartz Trigger 模板。 */
public final class QuartzTriggerTemplate {

    /** Trigger 类型。 */
    public enum TriggerType {
        /** CronTrigger。 */
        CRON,
        /** SimpleTrigger，可表达一次性或固定间隔。 */
        SIMPLE
    }

    /** Quartz 原生错过策略。 */
    public enum MisfirePolicy {
        /** 跳过错过的 Cron 周期。 */
        DO_NOTHING,
        /** 立即执行一次。 */
        FIRE_ONCE_NOW,
        /** 跳到下一个未来周期并保留剩余次数语义。 */
        NEXT_WITH_REMAINING_COUNT
    }

    private final TriggerType triggerType;
    private final String cronExpression;
    private final Duration interval;
    private final boolean oneShot;
    private final ZoneId timeZone;
    private final MisfirePolicy misfirePolicy;

    private QuartzTriggerTemplate(TriggerType triggerType, String cronExpression, Duration interval,
                                  boolean oneShot, ZoneId timeZone, MisfirePolicy misfirePolicy) {
        this.triggerType = triggerType;
        this.cronExpression = cronExpression;
        this.interval = interval;
        this.oneShot = oneShot;
        this.timeZone = timeZone;
        this.misfirePolicy = misfirePolicy;
        validate();
    }

    /** 创建 Cron 模板。 */
    public static QuartzTriggerTemplate cron(String cronExpression, ZoneId timeZone, MisfirePolicy misfirePolicy) {
        return new QuartzTriggerTemplate(TriggerType.CRON, cronExpression, null, false,
                timeZone, misfirePolicy == null ? MisfirePolicy.DO_NOTHING : misfirePolicy);
    }

    /** 创建一次性 Simple 模板。 */
    public static QuartzTriggerTemplate oneShot(MisfirePolicy misfirePolicy) {
        return new QuartzTriggerTemplate(TriggerType.SIMPLE, null, Duration.ofMillis(1), true,
                null, misfirePolicy == null ? MisfirePolicy.FIRE_ONCE_NOW : misfirePolicy);
    }

    /** 创建固定间隔 Simple 模板。 */
    public static QuartzTriggerTemplate fixedInterval(Duration interval, MisfirePolicy misfirePolicy) {
        return new QuartzTriggerTemplate(TriggerType.SIMPLE, null, interval, false,
                null, misfirePolicy == null ? MisfirePolicy.NEXT_WITH_REMAINING_COUNT : misfirePolicy);
    }

    /** 按模板生成绑定到指定 Job 的 Trigger。 */
    public Trigger build(TriggerKey triggerKey, JobKey jobKey) {
        return build(triggerKey, jobKey, null);
    }

    /**
     * 按模板生成绑定到指定 Job 的 Trigger，并可指定首次触发时间。
     *
     * @param triggerKey Trigger 的稳定标识
     * @param jobKey     要绑定的 Job 标识
     * @param startAt    首次触发时间；为空时从当前时间开始
     * @return 完整的 Quartz Trigger；输入有效时不会返回 null
     */
    public Trigger build(TriggerKey triggerKey, JobKey jobKey, Instant startAt) {
        var builder = TriggerBuilder.newTrigger().withIdentity(triggerKey).forJob(jobKey).withPriority(5);
        if (startAt == null) {
            builder.startNow();
        } else {
            builder.startAt(Date.from(startAt));
        }
        if (triggerType == TriggerType.CRON) {
            var schedule = CronScheduleBuilder.cronSchedule(cronExpression)
                    .inTimeZone(TimeZone.getTimeZone(timeZone));
            schedule = switch (misfirePolicy) {
                case DO_NOTHING -> schedule.withMisfireHandlingInstructionDoNothing();
                case FIRE_ONCE_NOW -> schedule.withMisfireHandlingInstructionFireAndProceed();
                case NEXT_WITH_REMAINING_COUNT -> schedule.withMisfireHandlingInstructionIgnoreMisfires();
            };
            return builder.withSchedule(schedule).build();
        }
        var schedule = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(interval.toMillis());
        if (oneShot) {
            schedule = schedule.withRepeatCount(0);
        } else {
            schedule = schedule.repeatForever();
        }
        schedule = switch (misfirePolicy) {
            case DO_NOTHING, NEXT_WITH_REMAINING_COUNT -> schedule.withMisfireHandlingInstructionNextWithRemainingCount();
            case FIRE_ONCE_NOW -> schedule.withMisfireHandlingInstructionFireNow();
        };
        return builder.withSchedule(schedule).build();
    }

    /** 校验模板的封闭类型和值域。 */
    private void validate() {
        if (triggerType == TriggerType.CRON) {
            if (cronExpression == null || cronExpression.isBlank() || timeZone == null) {
                throw new IllegalArgumentException("Cron Trigger 必须包含表达式和时区");
            }
        } else if (interval == null || interval.isZero() || interval.isNegative()) {
            throw new IllegalArgumentException("Simple Trigger 间隔必须大于 0");
        }
    }

    /** @return Trigger 类型 */
    public TriggerType triggerType() {
        return triggerType;
    }

    /** @return Cron 表达式 */
    public String cronExpression() {
        return cronExpression;
    }

    /** @return Simple 间隔 */
    public Duration interval() {
        return interval;
    }

    /** @return 是否为一次性 Trigger */
    public boolean oneShot() {
        return oneShot;
    }

    /** @return Cron IANA 时区 */
    public ZoneId timeZone() {
        return timeZone;
    }

    /** @return Quartz 错过策略 */
    public MisfirePolicy misfirePolicy() {
        return misfirePolicy;
    }
}
