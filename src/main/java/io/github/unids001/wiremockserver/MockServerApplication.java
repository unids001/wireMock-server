package io.github.unids001.wiremockserver;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

        prepareFileLayout(wiremockRoot);

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
        System.out.println("  Files:    " + wiremockRoot.resolve("files"));

        shutdownLatch.await();
    }

    private static void prepareFileLayout(Path wiremockRoot) throws IOException {
        Files.createDirectories(wiremockRoot);

        Path filesDirectory = wiremockRoot.resolve("files");
        Files.createDirectories(filesDirectory);

        Path wiremockFileSource = wiremockRoot.resolve("__files");
        if (Files.exists(wiremockFileSource) && Files.isSymbolicLink(wiremockFileSource)) {
            return;
        }

        if (Files.exists(wiremockFileSource) && Files.isDirectory(wiremockFileSource)) {
            mirrorDirectory(filesDirectory, wiremockFileSource);
            return;
        }

        try {
            Files.createSymbolicLink(wiremockFileSource, Paths.get("files"));
        } catch (UnsupportedOperationException | IOException | SecurityException ex) {
            Files.createDirectories(wiremockFileSource);
            mirrorDirectory(filesDirectory, wiremockFileSource);
            System.out.println("Symlinks are unavailable; mirrored wiremock/files into wiremock/__files instead.");
        }
    }

    private static void mirrorDirectory(Path source, Path destination) throws IOException {
        if (Files.exists(destination)) {
            try (var walk = Files.walk(destination)) {
                walk.sorted((left, right) -> right.compareTo(left))
                        .filter(path -> !path.equals(destination))
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        });
            } catch (RuntimeException ex) {
                if (ex.getCause() instanceof IOException ioException) {
                    throw ioException;
                }
                throw ex;
            }
        }

        Files.createDirectories(destination);

        try (var paths = Files.walk(source)) {
            paths.forEach(path -> {
                Path relative = source.relativize(path);
                Path target = destination.resolve(relative.toString());
                try {
                    if (Files.isDirectory(path)) {
                        Files.createDirectories(target);
                    } else {
                        Files.createDirectories(target.getParent());
                        Files.copy(path, target, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (RuntimeException ex) {
            if (ex.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw ex;
        }
    }

    private static String baseUrl(int port) {
        return "http://localhost:" + port;
    }
}
