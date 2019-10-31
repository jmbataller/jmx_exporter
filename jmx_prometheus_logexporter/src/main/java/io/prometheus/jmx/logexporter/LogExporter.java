package io.prometheus.jmx.logexporter;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import java.io.IOException;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogExporter extends TimerTask {

    private static final Logger logger;

    static {
        try {
            LogManager.getLogManager().readConfiguration(LogExporter.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "init logging system", e);
        }
        logger = Logger.getLogger(LogExporter.class.getCanonicalName());
    }

    @Override
    public void run() {
        logErrorAndContinueIfException(() -> logMetrics());
    }

    private Void logMetrics() {
        Enumeration<Collector.MetricFamilySamples> metricFamilySamplesEnumeration = CollectorRegistry.defaultRegistry.metricFamilySamples();
        while (metricFamilySamplesEnumeration.hasMoreElements()) {
            Collector.MetricFamilySamples metric = metricFamilySamplesEnumeration.nextElement();
            metric.samples.forEach(sample -> {
                String logEntry = logErrorAndContinueIfException(() -> isValid(metric, sample) ? LogEntryMetric.of(metric, sample).toString() : null);
                logger.info(logEntry);
            });
        }
        return null;
    }

    private boolean isValid(final Collector.MetricFamilySamples metric, final Collector.MetricFamilySamples.Sample sample) {
        return metric != null && metric.name != null && metric.type != null && sample != null
                ? true : false;
    }

    private <T> T logErrorAndContinueIfException(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Throwable ex) {
            ex.printStackTrace();
            logger.severe("Error logging metrics. " + ex.getMessage());
            return null;
        }
    }
}
