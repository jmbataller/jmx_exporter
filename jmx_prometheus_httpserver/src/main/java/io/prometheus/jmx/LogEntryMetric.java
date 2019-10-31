package io.prometheus.jmx;

import io.prometheus.client.Collector;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

public class LogEntryMetric {

    public static final DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm.SSS'Z'");
    public static final DecimalFormat decimalFormat ;

    static {
        decimalFormat = ((DecimalFormat) DecimalFormat.getInstance(Locale.ENGLISH));
        decimalFormat.applyPattern("#,###,###.##");
    }

    private String name;
    private String type;
    private String timestamp;
    private String value;
    private String labels;

    private LogEntryMetric(final String name, final String type, final String timestamp, final String value, final String labels) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
        this.labels = labels;
    }

    public static LogEntryMetric of(final Collector.MetricFamilySamples metric) {
        return new LogEntryMetric(metric.name,
                metric.type.name().toLowerCase(),
                formatTimestamp(metric.samples.get(0).timestampMs),
                formatValue(metric.samples.get(0).value),
                "");
    }

    private static String formatTimestamp(final Long timestampInMSecs) {
        LocalDateTime datetime = Optional.ofNullable(timestampInMSecs).map(t -> Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).toLocalDateTime()).orElse(LocalDateTime.now());
        return datetime.format(datetimeFormatter);
    }

    private static String formatValue(final double value) {
        return decimalFormat.format(value);
    }

    @Override
    public String toString() {
        return String.format(
                "{ \"metric\": { " +
                        "\"name\":\"%s\", " +
                        "\"type\":\"%s\", " +
                        "\"value\":%s, " +
                        "\"timestamp\":\"%s\", " +
                        "\"labels\":{%s} } }",
                name, type, value, timestamp, labels);
    }
}
