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

package com.devops00.spectra.framework.serialization.jackson;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * 使用不可变 {@link DateTimeFormatter} 实现的传统 {@link DateFormat} 适配器。
 *
 * <p>Jackson 的传统 {@code Date} serializer 要求接收 {@code DateFormat}，此适配器让格式定义保持
 * Java Time 的线程安全语义，同时保留既有 API 的字符串格式。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/8
 */
final class ThreadSafeDateFormat extends DateFormat {

    private static final long serialVersionUID = 1L;

    private final String pattern;

    private transient DateTimeFormatter baseFormatter;

    private transient volatile DateTimeFormatter formatter;

    private volatile ZoneId zoneId;

    ThreadSafeDateFormat(String pattern, ZoneId zoneId) {
        this.pattern = Objects.requireNonNull(pattern, "pattern");
        this.baseFormatter = DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId");
        this.formatter = baseFormatter.withZone(zoneId);
    }

    /**
     * 使用当前线程安全日期格式写入日期文本。
     *
     * @param date          待格式化的日期对象。
     * @param buffer        用于写入格式化结果的字符缓冲区。
     * @param fieldPosition 由 Java 日期格式 API 使用的字段位置状态。
     * @return 返回写入格式化日期文本后的同一个 {@code buffer}；输入日期或缓冲区无效时按 JDK 约定抛出异常，不返回 null。
     */
    @Override
    public StringBuffer format(Date date, StringBuffer buffer, FieldPosition fieldPosition) {
        Objects.requireNonNull(date, "date");
        buffer.append(formatter.format(date.toInstant()));
        return buffer;
    }

    /**
     * 从文本解析日期，并更新解析位置。
     *
     * @param source   待解析的日期文本或待复制的格式对象。
     * @param position 日期解析位置状态，用于报告解析进度。
     * @return 返回按当前格式和时区解析出的日期；文本无效时返回 null，并通过 {@code position} 标记解析错误。
     */
    @Override
    public Date parse(String source, ParsePosition position) {
        int start = position.getIndex();
        if (source == null || start < 0 || start >= source.length()) {
            position.setErrorIndex(Math.max(start, 0));
            return null;
        }
        try {
            TemporalAccessor parsed = formatter.parse(source.substring(start));
            Instant instant = toInstant(parsed);
            position.setIndex(source.length());
            return Date.from(instant);
        } catch (DateTimeException | ArithmeticException exception) {
            position.setErrorIndex(start);
            return null;
        }
    }

    /**
     * 设置日期格式使用的时区。
     *
     * @param timeZone 日期格式使用的时区；变更会影响序列化结果。
     */
    @Override
    public void setTimeZone(TimeZone timeZone) {
        ZoneId newZone = Objects.requireNonNull(timeZone, "timeZone").toZoneId();
        this.zoneId = newZone;
        this.formatter = baseFormatter.withZone(newZone);
    }

    /**
     * 获取或判断 Framework 的 getTimeZone 结果。
     *
     * @return 返回当前日期格式使用的时区；未显式设置时返回格式对象的默认时区，不返回 null。
     */
    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(zoneId);
    }

    /**
     * 创建当前日期格式的独立副本。
     *
     * @return 返回包含相同格式、时区和 Locale 配置的独立日期格式副本；副本与原对象互不共享可变状态，不返回 null。
     */
    @Override
    public Object clone() {
        return new ThreadSafeDateFormat(pattern, zoneId);
    }

    private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
        input.defaultReadObject();
        this.baseFormatter = DateTimeFormatter.ofPattern(pattern, Locale.ROOT);
        this.formatter = baseFormatter.withZone(zoneId);
    }

    private Instant toInstant(TemporalAccessor parsed) {
        if (parsed.isSupported(ChronoField.INSTANT_SECONDS)) {
            return Instant.from(parsed);
        }
        if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
            return OffsetDateTime.from(parsed).toInstant();
        }
        if (parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
            return LocalDateTime.from(parsed).atZone(zoneId).toInstant();
        }
        return LocalDate.from(parsed).atStartOfDay(zoneId).toInstant();
    }
}
