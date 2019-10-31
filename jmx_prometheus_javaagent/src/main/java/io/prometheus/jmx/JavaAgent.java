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
            LogEntryScheduler.schedule(config.scrapeIntervalInSecs);
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
                        "(\\d{1,5}):" +              // scrape interval in secs
                        "(.+)");                     // config file

        Matcher matcher = pattern.matcher(args);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Malformed arguments - " + args);
        }

        String givenScrapeIntervalInSecs = matcher.group(1);
        String givenConfigFile = matcher.group(2);

        int scrapeIntervalInSecs = Integer.parseInt(givenScrapeIntervalInSecs);

        return new Config(scrapeIntervalInSecs, givenConfigFile);
    }

    static class Config {
        int scrapeIntervalInSecs;
        String file;

        Config(final int scrapeIntervalInSecs, final String file) {
            this.scrapeIntervalInSecs = scrapeIntervalInSecs;
            this.file = file;
        }
    }
}
