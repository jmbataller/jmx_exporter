package io.prometheus.jmx.logexporter;

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

    private static Logger logger;

    static {
        InputStream inputStream = LogExporter.class.getResourceAsStream("/logging.properties");
        if (null != inputStream) {
            try {
                LogManager.getLogManager().readConfiguration(inputStream);
            } catch (IOException e) {
                Logger.getGlobal().log(Level.SEVERE, "init logging system", e);
            }
            logger = Logger.getLogger(LogExporter.class.getCanonicalName());
        }
    }

    @Override
    public void run() {
        logMetrics();
    }

    private void logMetrics() {
        Enumeration<Collector.MetricFamilySamples> metricFamilySamplesEnumeration = CollectorRegistry.defaultRegistry.metricFamilySamples();
        while(metricFamilySamplesEnumeration.hasMoreElements()) {
            Collector.MetricFamilySamples metric = metricFamilySamplesEnumeration.nextElement();

            /**
             * metric.name,
             *                 metric.type.name().toLowerCase(),
             *                 formatTimestamp(metric.samples.get(0).timestampMs),
             *                 formatValue(metric.samples.get(0).value)
             */
            if(metric != null && metric.name != null && metric.type != null && metric.samples.size() > 0 && metric.samples.get(0) != null) {
                logger.info(LogEntryMetric.of(metric).toString());
                System.out.println(LogEntryMetric.of(metric).toString());
            }
        }
    }
}
