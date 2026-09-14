package io.github.unids001.wiremockserver;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

final class MockServerConfiguration {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_ROOT_DIR = "wiremock";
    private static final String PORT_ENV = "MOCK_SERVER_PORT";
    private static final String ROOT_ENV = "MOCK_SERVER_ROOT";

    private final int port;
    private final String rootDirectory;

    private MockServerConfiguration(int port, String rootDirectory) {
        this.port = port;
        this.rootDirectory = rootDirectory;
    }

    static MockServerConfiguration from(String[] args, Map<String, String> env) {
        Map<String, String> cli = parseArgs(args);

        int port = parsePort(firstNonBlank(
                cli.get("port"),
                env.get(PORT_ENV),
                env.get("PORT"),
                String.valueOf(DEFAULT_PORT)
        ));

        String rootDirectory = firstNonBlank(
                cli.get("root"),
                env.get(ROOT_ENV),
                DEFAULT_ROOT_DIR
        );

        return new MockServerConfiguration(port, rootDirectory);
    }

    int port() {
        return port;
    }

    String rootDirectory() {
        return rootDirectory;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (!arg.startsWith("-")) {
                continue;
            }

            String normalized = arg.replaceFirst("^-+", "");
            String key;
            String value;
            int equalsIndex = normalized.indexOf('=');
            if (equalsIndex >= 0) {
                key = normalized.substring(0, equalsIndex).toLowerCase(Locale.ROOT);
                value = normalized.substring(equalsIndex + 1);
            } else {
                key = normalized.toLowerCase(Locale.ROOT);
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    value = args[++i];
                } else {
                    value = "true";
                }
            }

            if ("p".equals(key)) {
                key = "port";
            }
            if ("r".equals(key)) {
                key = "root";
            }
            values.put(key, value);
        }
        return values;
    }

    private static int parsePort(String value) {
        try {
            int port = Integer.parseInt(value);
            if (port < 1 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535: " + value);
            }
            return port;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid port value: " + value, ex);
        }
    }

    @SafeVarargs
    private static <T> T firstNonBlank(T... values) {
        for (T value : values) {
            if (value instanceof String stringValue) {
                if (!stringValue.isBlank()) {
                    return value;
                }
            } else if (value != null) {
                return value;
            }
        }
        return null;
    }
}
