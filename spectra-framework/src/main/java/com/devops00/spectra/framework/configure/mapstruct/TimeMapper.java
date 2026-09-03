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

package com.devops00.spectra.framework.configure.mapstruct;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * 时间处理 mapper
 * <p>
 * 这个 mapper 主要是处理 Instant 转换到 LocalDateTime等对外的类型.
 * <p>
 * 可以动态获取时区进行设置,具体获取时区代码还在处理
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/10 11:13
 */
@Component
public class TimeMapper {

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 延迟解析安全上下文，打断安全会话适配器与 MapStruct 时间转换器之间的启动期依赖环。
     * <p>
     * TimeMapper 仍在实际转换时读取当前用户时区，不改变原有业务行为；这里只避免在 Spring 创建
     * UserOnlineConverter 时提前反向创建 SecurityContextAccessor。
     *
     * @param securityContextAccessor 当前安全上下文访问器
     */
    public TimeMapper(@Lazy SecurityContextAccessor securityContextAccessor) {
        this.securityContextAccessor = securityContextAccessor;
    }

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * 当前用户时区
     *
     * @return 时区
     */
    public ZoneId getUserZoneId() {
        return ZoneId.of(securityContextAccessor.currentUserZoneId());
        //return ZoneId.of("UTC");
    }

    /**
     * Instant 转 LocalDateTime
     *
     * @param instant {@link Instant}
     * @return {@link LocalDateTime}
     */
    public @Nullable LocalDateTime toLocalDateTime(@Nullable Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, getUserZoneId());
    }

    /**
     * Unix 时间戳转 LocalDateTime。
     *
     * @param epochMilli Unix 毫秒时间戳
     * @return {@link LocalDateTime}
     */
    public @Nullable LocalDateTime toLocalDateTime(@Nullable Long epochMilli) {
        return epochMilli == null ? null : toLocalDateTime(Instant.ofEpochMilli(epochMilli));
    }

    /**
     * Date 转 LocalDateTime
     *
     * @param date {@link Date}
     * @return {@link LocalDateTime}
     */
    public @Nullable LocalDateTime toLocalDateTime(@Nullable Date date) {
        return date == null ? null : toLocalDateTime(date.toInstant());
    }

    /**
     * LocalDateTime 转 Instant
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(getUserZoneId()).toInstant();
    }

    /**
     * Instant 转 LocalDate
     *
     * @param instant {@link Instant}
     * @return {@link LocalDate}
     */
    public @Nullable LocalDate toLocalDate(@Nullable Instant instant) {
        return instant == null ? null : instant.atZone(getUserZoneId()).toLocalDate();
    }

    /**
     * LocalDate 转 Instant
     *
     * @param localDate {@link LocalDate}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable LocalDate localDate) {
        return localDate == null ? null : localDate.atStartOfDay(getUserZoneId()).toInstant();
    }

    /**
     * Instant 转 LocalTime
     *
     * @param instant {@link Instant}
     * @return {@link LocalTime}
     */
    public @Nullable LocalTime toLocalTime(@Nullable Instant instant) {
        return instant == null ? null : instant.atZone(getUserZoneId()).toLocalTime();
    }

    /**
     * LocalTime 转 Instant
     * Instant 需要日期，这里默认用今天
     *
     * @param localTime {@link LocalTime}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable LocalTime localTime) {
        return localTime == null ? null : LocalDate.now(getUserZoneId()).atTime(localTime).atZone(getUserZoneId()).toInstant();
    }

    /**
     * Instant 转 ZonedDateTime
     *
     * @param instant {@link Instant}
     * @return {@link ZonedDateTime}
     */
    public @Nullable ZonedDateTime toZonedDateTime(@Nullable Instant instant) {
        return instant == null ? null : instant.atZone(getUserZoneId());
    }

    /**
     * ZonedDateTime 转 Instant
     *
     * @param zonedDateTime {@link ZonedDateTime}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : zonedDateTime.toInstant();
    }

    /**
     * Instant 转 OffsetDateTime
     *
     * @param instant {@link Instant}
     * @return {@link OffsetDateTime}
     */
    public @Nullable OffsetDateTime toOffsetDateTime(@Nullable Instant instant) {
        return instant == null ? null : instant.atZone(getUserZoneId()).toOffsetDateTime();
    }

    /**
     * OffsetDateTime 转 Instant
     *
     * @param offsetDateTime {@link OffsetDateTime}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null : offsetDateTime.toInstant();
    }

    /**
     * Instant 转 Date
     *
     * @param instant {@link Instant}
     * @return {@link Date}
     */
    public @Nullable Date toDate(@Nullable Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    /**
     * Date 转 Instant
     *
     * @param date {@link Date}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable Date date) {
        return date == null ? null : date.toInstant();
    }

    /**
     * Instant 转 Timestamp
     *
     * @param instant {@link Instant}
     * @return {@link Timestamp}
     */
    public @Nullable Timestamp toTimestamp(@Nullable Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    /**
     * Timestamp 转 Instant
     *
     * @param timestamp {@link Timestamp}
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    /**
     * Instant 转时间戳
     *
     * @param instant {@link Instant}
     * @return 时间戳
     */
    public @Nullable Long toEpochMilli(@Nullable Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    /**
     * 时间戳转 Instant
     *
     * @param epochMilli 时间戳
     * @return {@link Instant}
     */
    public @Nullable Instant toInstant(@Nullable Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli);
    }

    /**
     * Instant转换为 ISO 8601 格式字符串
     *
     * @param instant {@link Instant}
     * @return ISO 8601 格式字符串时间
     */
    public @Nullable String toString(@Nullable Instant instant) {
        return instant == null ? null : ISO_FORMATTER.format(instant.atZone(getUserZoneId()));
    }

    /**
     * ISO 8601格式字符串转换到Instant。
     * <p>
     * 支持带偏移量/时区的日期时间、无时区日期时间、日期和时间字符串。
     * 无时区字符串按当前用户时区解释；日期默认当天开始，时间默认当前用户时区的当天。
     *
     * @param text IOS 8601 格式的字符串
     * @return Instant
     */
    public @Nullable Instant toInstant(@Nullable String text) {
        if (text == null || text.isBlank())
            return null;

        var value = text.trim();
        try {
            return ZonedDateTime.parse(value, ISO_FORMATTER).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他 ISO 8601 形式。
        }
        try {
            return OffsetDateTime.parse(value, ISO_FORMATTER).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其他 ISO 8601 形式。
        }
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(getUserZoneId()).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试日期或时间形式。
        }
        try {
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(getUserZoneId()).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试时间形式。
        }
        try {
            return OffsetTime.parse(value, DateTimeFormatter.ISO_OFFSET_TIME).atDate(LocalDate.now(getUserZoneId())).toInstant();
        } catch (DateTimeParseException ignored) {
            // 继续尝试无时区时间形式。
        }
        return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME).atDate(LocalDate.now(getUserZoneId())).atZone(getUserZoneId()).toInstant();
    }
}
