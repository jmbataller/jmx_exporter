package io.prometheus.jmx;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.jmx.logexporter.LogEntryScheduler;

public class JavaAgent {

    public static void agentmain(String agentArgument, Instrumentation instrumentation) throws Exception {
        premain(agentArgument, instrumentation);
    }

    public static void premain(String agentArgument, Instrumentation instrumentation) throws Exception {

        try {
            Config config = parseConfig(agentArgument);

            new BuildInfoCollector().register();
            new JmxCollector(new File(config.file)).register();
            DefaultExports.initialize();
            LogEntryScheduler.schedule(config.scrapeIntervalInMillis);
        }
        catch (IllegalArgumentException e) {
            System.err.println("Usage: -javaagent:/path/to/JavaAgent.jar=<scrape interval in secs>:<yaml configuration file> " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Parse the Java Agent configuration. The arguments are typically specified to the JVM as a javaagent as
     * {@code -javaagent:/path/to/agent.jar=<CONFIG>}. This method parses the {@code <CONFIG>} portion.
     * @param args provided agent args
     * @return configuration to use for our application
     */
    public static Config parseConfig(String args) {
        Pattern pattern = Pattern.compile(
                        "(\\d{1,10}):" +              // scrape interval in secs
                        "(.+)");                     // config file

        Matcher matcher = pattern.matcher(args);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed arguments - " + args);
        }

        String givenScrapeIntervalInSecs = matcher.group(1);
        String givenConfigFile = matcher.group(2);

        int scrapeIntervalInMillis = Integer.parseInt(givenScrapeIntervalInSecs) * 1000;

        return new Config(scrapeIntervalInMillis, givenConfigFile);
    }

    static class Config {
        int scrapeIntervalInMillis;
        String file;

        Config(final int scrapeIntervalInMillis, final String file) {
            this.scrapeIntervalInMillis = scrapeIntervalInMillis;
            this.file = file;
        }
    }
}
