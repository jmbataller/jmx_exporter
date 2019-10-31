package io.prometheus.jmx.logexporter;

import java.util.Timer;

/**
 * Scheduler will run the task for first time after the specified `delayInMillis` and every `delayInMillis`
 */
public class LogEntryScheduler {

    private final Timer timer;
    private final LogExporter logExporter;

    private LogEntryScheduler(final long delayInMillis) {
        this.timer = new Timer();
        this.logExporter = new LogExporter();
        timer.scheduleAtFixedRate(logExporter, delayInMillis, delayInMillis);
    }

    public static LogEntryScheduler schedule(final long delayInMillis) {
        return new LogEntryScheduler(delayInMillis);
    }
}
