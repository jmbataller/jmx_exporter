package io.prometheus.jmx.logexporter;

import io.prometheus.client.Collector;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Collectors;

public class LogEntryMetric {

    private String name;
    private String type;
    private String timestamp;
    private Double value;
    private String labels;

    private LogEntryMetric(final String name, final String type, final String timestamp, final Double value, final String labels) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
        this.labels = labels;
    }

    public static LogEntryMetric of(final Collector.MetricFamilySamples metric) {
        return new LogEntryMetric(metric.name, convertType(metric.type),
                formatTimestamp(metric.samples.get(0).timestampMs), metric.samples.get(0).value,
                "");
    }

    private static String formatTimestamp(final Long timestampInMSecs) {
        LocalDateTime datetime = Optional.ofNullable(timestampInMSecs).map(t -> Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).toLocalDateTime()).orElse(LocalDateTime.now());
        return datetime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm.SSS'Z'"));
    }

    private static String convertType(final Collector.Type type) {
        return Optional.ofNullable(type)
                .filter(t -> Collector.Type.GAUGE.equals(t) || Collector.Type.COUNTER.equals(t))
                .map(t -> t.name().toLowerCase())
                .orElseGet(() -> Collector.Type.SUMMARY.name().toLowerCase());
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
