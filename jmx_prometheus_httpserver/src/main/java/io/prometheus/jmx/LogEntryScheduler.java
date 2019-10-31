package io.prometheus.jmx;

import java.util.Timer;

public class LogEntryScheduler {

    private final Timer timer;
    private final LogExporter logExporter;

    private LogEntryScheduler(final long delayInMillis) {
        this.timer = new Timer();
        this.logExporter = new LogExporter();
        timer.scheduleAtFixedRate(logExporter, 0, delayInMillis);
    }

    public static LogEntryScheduler schedule(final long delayInMillis) {
        return new LogEntryScheduler(delayInMillis);
    }
}
