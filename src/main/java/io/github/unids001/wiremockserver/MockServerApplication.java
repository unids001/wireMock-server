package io.github.unids001.wiremockserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;

public final class MockServerApplication {
    private static final int DEFAULT_PORT = 8080;
    private static final String DEFAULT_ROOT_DIR = "wiremock";
    private static final String PORT_ENV = "MOCK_SERVER_PORT";
    private static final String ROOT_ENV = "MOCK_SERVER_ROOT";

    private MockServerApplication() {
    }

    public static void main(String[] args) throws Exception {
        MockServerConfiguration configuration = MockServerConfiguration.from(args, System.getenv());
        Path wiremockRoot = Paths.get(configuration.rootDirectory()).toAbsolutePath().normalize();

        WireMockServer server = new WireMockServer(
                WireMockConfiguration.options()
                        .port(configuration.port())
                        .usingFilesUnderDirectory(wiremockRoot.toString())
                        .stubCorsEnabled(true)
        );

        CountDownLatch shutdownLatch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping mock server...");
            server.stop();
            shutdownLatch.countDown();
        }, "mock-server-shutdown"));

        server.start();

        System.out.println("Mock server started");
        System.out.println("  Base URL: " + baseUrl(server.port()));
        System.out.println("  Health:   " + baseUrl(server.port()) + "/health");
        System.out.println("  Admin:    " + baseUrl(server.port()) + "/__admin/");
        System.out.println("  Mappings: " + wiremockRoot.resolve("mappings"));
        System.out.println("  Files:    " + wiremockRoot.resolve("__files"));

        shutdownLatch.await();
    }

    private static String baseUrl(int port) {
        return "http://localhost:" + port;
    }
}
