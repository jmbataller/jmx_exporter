package io.prometheus.jmx;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogExporter extends TimerTask {

    private static final Logger logger;

    static {
        InputStream inputStream = LogExporter.class.getResourceAsStream("/logging.properties");
        try {
            LogManager.getLogManager().readConfiguration(inputStream);
        } catch (IOException e) {
            Logger.getGlobal().log(Level.SEVERE, "init logging system", e);
        }
        logger = Logger.getLogger(LogExporter.class.getCanonicalName());
    }

    @Override
    public void run() {
        logMetrics();
    }

    private void logMetrics() {
        Enumeration<Collector.MetricFamilySamples> metricFamilySamplesEnumeration = CollectorRegistry.defaultRegistry.metricFamilySamples();
        while(metricFamilySamplesEnumeration.hasMoreElements()) {
            Collector.MetricFamilySamples metric = metricFamilySamplesEnumeration.nextElement();
            logger.info(LogEntryMetric.of(metric).toString());
        }
    }
}
