package io.github.yangxj96.spectra.core.configure.mapstruct;


import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 时间处理 mapper <br/>
 * 这个 mapper 主要是处理 Instant 转换到 LocalDateTime等对外的类型.<br/>
 * 可以动态获取时区进行设置,具体获取时区代码还在处理<br/>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/10 11:13
 */
@Component
public class TimeMapper {

    /**
     * 当前用户时区
     *
     * @return 时区
     */
    public ZoneId getUserZoneId() {
        // return ZoneId.of("Asia/Shanghai");
        return ZoneId.of("UTC");
    }


    /* --------------------------------------
       Instant <-> LocalDateTime
    -------------------------------------- */

    public LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null ? null : LocalDateTime.ofInstant(instant, getUserZoneId());
    }

    public Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null ? null :
                localDateTime.atZone(getUserZoneId()).toInstant();
    }

    /* --------------------------------------
       Instant <-> LocalDate
    -------------------------------------- */

    public LocalDate toLocalDate(Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId()).toLocalDate();
    }

    public Instant toInstant(LocalDate localDate) {
        return localDate == null ? null :
                localDate.atStartOfDay(getUserZoneId()).toInstant();
    }

    /* --------------------------------------
       Instant <-> LocalTime
    -------------------------------------- */

    public LocalTime toLocalTime(Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId()).toLocalTime();
    }

    /**
     * LocalTime -> Instant 需要日期，这里默认用今天
     * 你可以根据业务改成抛异常
     */
    public Instant toInstant(LocalTime localTime) {
        return localTime == null ? null :
                LocalDate.now(getUserZoneId())
                        .atTime(localTime)
                        .atZone(getUserZoneId())
                        .toInstant();
    }

    /* --------------------------------------
       Instant <-> ZonedDateTime
    -------------------------------------- */

    public ZonedDateTime toZonedDateTime(Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId());
    }

    public Instant toInstant(ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null :
                zonedDateTime.toInstant();
    }

    /* --------------------------------------
       Instant <-> OffsetDateTime
    -------------------------------------- */

    public OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant == null ? null :
                instant.atZone(getUserZoneId()).toOffsetDateTime();
    }

    public Instant toInstant(OffsetDateTime offsetDateTime) {
        return offsetDateTime == null ? null :
                offsetDateTime.toInstant();
    }

    /* --------------------------------------
       Instant <-> Date
    -------------------------------------- */

    public Date toDate(Instant instant) {
        return instant == null ? null : Date.from(instant);
    }

    public Instant toInstant(Date date) {
        return date == null ? null : date.toInstant();
    }

    /* --------------------------------------
       Instant <-> Timestamp
    -------------------------------------- */

    public Timestamp toTimestamp(Instant instant) {
        return instant == null ? null : Timestamp.from(instant);
    }

    public Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }

    /* --------------------------------------
       Instant <-> epoch milli
    -------------------------------------- */

    public Long toEpochMilli(Instant instant) {
        return instant == null ? null : instant.toEpochMilli();
    }

    public Instant toInstant(Long epochMilli) {
        return epochMilli == null ? null : Instant.ofEpochMilli(epochMilli);
    }

    /* --------------------------------------
       Instant <-> String (ISO 8601)
    -------------------------------------- */

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public String toString(Instant instant) {
        return instant == null ? null : ISO_FORMATTER.format(instant.atZone(getUserZoneId()));
    }

    public Instant toInstant(String text) {
        if (text == null || text.isBlank()) return null;
        LocalDateTime ldt = LocalDateTime.parse(text, ISO_FORMATTER);
        return ldt.atZone(getUserZoneId()).toInstant();
    }

}
