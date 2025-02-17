package io.prometheus.jmx;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;

public class JavaAgentIT {
    private List<URL> getClassloaderUrls() {
        return getClassloaderUrls(getClass().getClassLoader());
    }

    private static List<URL> getClassloaderUrls(ClassLoader classLoader) {
        if (classLoader == null) {
            return Collections.emptyList();
        }
        if (!(classLoader instanceof URLClassLoader)) {
            return getClassloaderUrls(classLoader.getParent());
        }
        URLClassLoader u = (URLClassLoader) classLoader;
        List<URL> result = new ArrayList<URL>(Arrays.asList(u.getURLs()));
        result.addAll(getClassloaderUrls(u.getParent()));
        return result;
    }

    private String buildClasspath() {
        StringBuilder sb = new StringBuilder();
        for (URL url : getClassloaderUrls()) {
            if (!url.getProtocol().equals("file")) {
                continue;
            }
            if (sb.length() != 0) {
                sb.append(java.io.File.pathSeparatorChar);
            }
            sb.append(url.getPath());
        }
        return sb.toString();
    }

    @Test
    public void agentLoads() throws IOException, InterruptedException {
        // If not starting the testcase via Maven, set the buildDirectory and finalName system properties manually.
        final String buildDirectory = (String) System.getProperties().get("buildDirectory");
        final String finalName = (String) System.getProperties().get("finalName");
        final int scrapeIntervalInSecs = 1;
        final String config = resolveRelativePathToResource("test.yml");
        final String javaagent = "-javaagent:" + buildDirectory + "/" + finalName + ".jar=" + scrapeIntervalInSecs + ":" + config;

        final String javaHome = System.getenv("JAVA_HOME");
        final String java;
        if (javaHome != null && javaHome.equals("")) {
            java = javaHome + "/bin/java";
        } else {
            java = "java";
        }

        final Process app = new ProcessBuilder()
                .command(java, javaagent, "-cp", buildClasspath(), "io.prometheus.jmx.TestApplication")
                .start();
        try {
            TimeUnit.SECONDS.sleep(scrapeIntervalInSecs + 1);

            // Tell application to stop
            app.getOutputStream().write('\n');
            try {
                app.getOutputStream().flush();
            } catch (IOException ignored) {
            }
        } finally {
            app.destroy();
            final int exitcode = app.waitFor();
            // Log any errors printed
            int len;
            byte[] buffer = new byte[100];
            while ((len = app.getErrorStream().read(buffer)) != -1) {
                System.out.write(buffer, 0, len);
            }

            final BufferedReader reader = new BufferedReader(new InputStreamReader(app.getInputStream()));
            StringBuilder builder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.getProperty("line.separator"));
            }
            String result = builder.toString();

            assertThat("Application did not exit cleanly", exitcode == 0);
        }
    }

    //trying to avoid the occurrence of any : in the windows path
    private String resolveRelativePathToResource(String resource) {
        final String configwk = new File(getClass().getClassLoader().getResource(resource).getFile()).getAbsolutePath();
        final File workingDir = new File(new File(".").getAbsolutePath());
        return "." + configwk.replace(workingDir.getParentFile().getAbsolutePath(), "");
    }
}
