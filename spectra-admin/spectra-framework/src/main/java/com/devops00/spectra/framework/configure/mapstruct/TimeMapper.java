package com.devops00.spectra.framework.configure.mapstruct;


import com.devops00.spectra.security.base.holder.SecUtil;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/// 时间处理 mapper
///
/// 这个 mapper 主要是处理 Instant 转换到 LocalDateTime等对外的类型.
///
/// 可以动态获取时区进行设置,具体获取时区代码还在处理
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/10 11:13
@Component
@SuppressWarnings("unused")
public class TimeMapper {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /// 当前用户时区
    ///
    /// @return 时区
    public ZoneId getUserZoneId() {
        return ZoneId.of(SecUtil.getCurrentUserZoneId());
        //return ZoneId.of("UTC");
    }

    /// Instant 转 LocalDateTime
    ///
    /// @param instant {@link Instant}
    /// @return {@link LocalDateTime}
    public @Nullable LocalDateTime toLocalDateTime(@Nullable Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, getUserZoneId());
    }

    /// LocalDateTime 转 Instant
    ///
    /// @param localDateTime {@link LocalDateTime}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable LocalDateTime localDateTime) {
        return localDateTime == null ? null :
                localDateTime.atZone(getUserZoneId()).toInstant();
    }

    /// Instant 转 LocalDate
    ///
    /// @param instant {@link Instant}
    /// @return {@link LocalDate}
    public @Nullable LocalDate toLocalDate(@Nullable Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId()).toLocalDate();
    }

    /// LocalDate 转 Instant
    ///
    /// @param localDate {@link LocalDate}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable LocalDate localDate) {
        return localDate == null ? null :
                localDate.atStartOfDay(getUserZoneId()).toInstant();
    }

    /// Instant 转 LocalTime
    ///
    /// @param instant {@link Instant}
    /// @return {@link LocalTime}
    public @Nullable LocalTime toLocalTime(@Nullable Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId()).toLocalTime();
    }

    /// LocalTime 转 Instant
    /// Instant 需要日期，这里默认用今天
    ///
    /// @param localTime {@link LocalTime}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable LocalTime localTime) {
        return localTime == null ? null :
                LocalDate.now(getUserZoneId())
                        .atTime(localTime)
                        .atZone(getUserZoneId())
                        .toInstant();
    }

    /// Instant 转 ZonedDateTime
    ///
    /// @param instant {@link Instant}
    /// @return {@link ZonedDateTime}
    public @Nullable ZonedDateTime toZonedDateTime(@Nullable Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId());
    }

    /// ZonedDateTime 转 Instant
    ///
    /// @param zonedDateTime {@link ZonedDateTime}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null :
                zonedDateTime.toInstant();
    }

    /// Instant 转 OffsetDateTime
    ///
    /// @param instant {@link Instant}
    /// @return {@link OffsetDateTime}
    public @Nullable OffsetDateTime toOffsetDateTime(@Nullable Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId()).toOffsetDateTime();
    }

    /// OffsetDateTime 转 Instant
    ///
    /// @param offsetDateTime {@link OffsetDateTime}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null :
                offsetDateTime.toInstant();
    }

    /// Instant 转 Date
    ///
    /// @param instant {@link Instant}
    /// @return {@link Date}
    public @Nullable Date toDate(@Nullable Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    /// Date 转 Instant
    ///
    /// @param date {@link Date}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable Date date) {
        return date == null ? null : date.toInstant();
    }

    /// Instant 转 Timestamp
    ///
    /// @param instant {@link Instant}
    /// @return {@link Timestamp}
    public @Nullable Timestamp toTimestamp(@Nullable Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    /// Timestamp 转 Instant
    ///
    /// @param timestamp {@link Timestamp}
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    /// Instant 转时间戳
    ///
    /// @param instant {@link Instant}
    /// @return 时间戳
    public @Nullable Long toEpochMilli(@Nullable Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    /// 时间戳转 Instant
    ///
    /// @param epochMilli 时间戳
    /// @return {@link Instant}
    public @Nullable Instant toInstant(@Nullable Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli);
    }

    /// Instant转换为 ISO 8601 格式字符串
    ///
    /// @param instant {@link Instant}
    /// @return ISO 8601 格式字符串时间
    public @Nullable String toString(@Nullable Instant instant) {
        return instant == null ? null : ISO_FORMATTER.format(instant.atZone(getUserZoneId()));
    }

    /// IOS 8601格式字符串转换到Instant
    ///
    /// @param text IOS 8601 格式的字符串
    /// @return Instant
    public @Nullable Instant toInstant(@Nullable String text) {
        if (text == null || text.isBlank()) return null;
        LocalDateTime ldt = LocalDateTime.parse(text, ISO_FORMATTER);
        return ldt.atZone(getUserZoneId()).toInstant();
    }

}
