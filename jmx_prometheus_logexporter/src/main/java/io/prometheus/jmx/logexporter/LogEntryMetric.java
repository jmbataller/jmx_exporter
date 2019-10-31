package io.prometheus.jmx.logexporter;

import io.prometheus.client.Collector;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LogEntryMetric {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private String name;
    private String type;
    private String timestamp;
    private Double value;
    private Map<String, String> labels;

    private LogEntryMetric(final String name, final String type, final String timestamp, final Double value, final Map<String, String> labels) {
        this.name = name;
        this.type = type;
        this.timestamp = timestamp;
        this.value = value;
        this.labels = labels;
    }

    public static LogEntryMetric of(final Collector.MetricFamilySamples metric, final Collector.MetricFamilySamples.Sample sample) {
        return new LogEntryMetric(metric.name, convertType(metric.type),
                formatTimestamp(metric.samples.get(0).timestampMs), sample.value,
                convertLabels(sample.labelNames, sample.labelValues));
    }

    private static String formatTimestamp(final Long timestampInMSecs) {
        LocalDateTime datetime = Optional.ofNullable(timestampInMSecs).map(t -> Instant.ofEpochMilli(t).atZone(ZoneId.systemDefault()).toLocalDateTime()).orElse(LocalDateTime.now());
        return datetime.format(dateTimeFormatter);
    }

    private static String convertType(final Collector.Type type) {
        return Optional.ofNullable(type)
                .filter(t -> Collector.Type.GAUGE.equals(t) || Collector.Type.COUNTER.equals(t))
                .map(t -> t.name().toLowerCase())
                .orElseGet(() -> Collector.Type.SUMMARY.name().toLowerCase());
    }

    private static Map<String, String> convertLabels(final List<String> namesList, final List<String> valuesList) {
        if (namesList == null || valuesList == null || namesList.isEmpty() || valuesList.isEmpty()) return Collections.emptyMap();
        Map<String, String> labels = new HashMap<>();
        Iterator<String> names = namesList.iterator();
        Iterator<String> values = valuesList.iterator();
        while (names.hasNext() && values.hasNext()) {
            labels.put(names.next(), values.next());
        }
        return labels;
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
                name, type, value, timestamp, labelsToString(labels));
    }

    private static String labelsToString(final Map<String, String> labelsMap) {
        if (labelsMap.isEmpty()) return "";
        String labels = "";
        for (Map.Entry<String, String> entry : labelsMap.entrySet()) {
            labels += (labels.length() != 0 ? ", " : "") + String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue());
        }
        return labels;
    }
}
