package io.prometheus.jmx;

import org.junit.Assert;
import org.junit.Test;

public class JavaAgentTest {
    /**
     * Test that the agent string argument is parsed properly. We expect the agent argument in one of these forms...
     * <pre>
     * {@code <scrape interval in secs>:<yaml configuration file>}
     * </pre>
     * Since the ':' character is part of the spec for this arg, Windows-style paths could cause an issue with parsing.
     * See https://github.com/prometheus/jmx_exporter/issues/312.
     */
    @Test
    public void testAgentStringParsing() {

        JavaAgent.Config config = JavaAgent.parseConfig("60:config.yaml");
        Assert.assertEquals(60, config.scrapeIntervalInSecs);
        Assert.assertEquals("config.yaml", config.file);

        config = JavaAgent.parseConfig("10:\\Windows\\Local\\Drive\\Path\\config.yaml");
        Assert.assertEquals(10, config.scrapeIntervalInSecs);
        Assert.assertEquals("\\Windows\\Local\\Drive\\Path\\config.yaml", config.file);

        // the following check was previously failing to parse the file correctly
        config = JavaAgent.parseConfig("10:C:\\Windows\\Path\\config.yaml");
        Assert.assertEquals(10, config.scrapeIntervalInSecs);
        Assert.assertEquals("C:\\Windows\\Path\\config.yaml", config.file);

        // the following check was previously failing to parse the file correctly
        config = JavaAgent.parseConfig("10:C:\\Windows\\Path\\config.yaml");
        Assert.assertEquals(10, config.scrapeIntervalInSecs);
        Assert.assertEquals("C:\\Windows\\Path\\config.yaml", config.file);

        // the following check was previously failing to parse the file correctly
        config = JavaAgent.parseConfig("10:C:\\Windows\\Path\\config.yaml");
        Assert.assertEquals(10, config.scrapeIntervalInSecs);
        Assert.assertEquals("C:\\Windows\\Path\\config.yaml", config.file);
    }

    /**
     * If someone is specifying an ipv6 address and a host name, this should be rejected.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testRejectInvalidInput() {
        JavaAgent.parseConfig("invalidScrapeInterval:config.yaml");
    }
}
