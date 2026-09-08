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

    @Override
    public StringBuffer format(Date date, StringBuffer buffer, FieldPosition fieldPosition) {
        Objects.requireNonNull(date, "date");
        buffer.append(formatter.format(date.toInstant()));
        return buffer;
    }

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

    @Override
    public void setTimeZone(TimeZone timeZone) {
        ZoneId newZone = Objects.requireNonNull(timeZone, "timeZone").toZoneId();
        this.zoneId = newZone;
        this.formatter = baseFormatter.withZone(newZone);
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(zoneId);
    }

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
