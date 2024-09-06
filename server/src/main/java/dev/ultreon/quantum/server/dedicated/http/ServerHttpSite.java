package dev.ultreon.quantum.server.dedicated.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.server.QuantumServer;
import dev.ultreon.quantum.server.dedicated.ServerConfig;
import dev.ultreon.quantum.server.dedicated.ServerPlatform;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Executors;

public class ServerHttpSite implements AutoCloseable {
    private static final ApiHandler apiHandler = new ApiHandler();
    private static ServerHttpSite instance;
    public static String serverName = "Quantum Dedicated Server";
    public static String serverVersion = "1.0.0";
    public static String serverDescription = "A server for the Quantum Voxel game";

    public ServerHttpSite() throws IOException {
        this.prepare();

        CommonConstants.LOGGER.info("Starting HTTP server...");

        HttpServer httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(80), 0);
        httpServer.setExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        httpServer.start();

        httpServer.createContext("/api/", apiHandler);

        httpServer.createContext("/dl/", new PasswordProtectedHandler(exchange -> {
            // Download file from server
            String path1 = exchange.getRequestURI().getPath();
            if (!path1.startsWith("/dl/") || path1.contains("..")) {
                CommonConstants.LOGGER.error("Invalid path: " + path1);
                exchange.sendResponseHeaders(HttpCodes.FORBIDDEN, 0);
                return;
            }
            Path path = Path.of(path1);
            if (path.isAbsolute()) {
                CommonConstants.LOGGER.error("Invalid path: " + path);
                exchange.sendResponseHeaders(HttpCodes.FORBIDDEN, 0);
                return;
            }

            if (Files.notExists(path)) {
                CommonConstants.LOGGER.error("File not found: " + path);
                exchange.sendResponseHeaders(HttpCodes.NOT_FOUND, 0);
                return;
            }

            // Check for any symbolic links
            Path cur = path;
            while (true) {
                cur = cur.getParent();

                if (cur == null) {
                    break;
                }

                if (Files.isSymbolicLink(cur)) {
                    exchange.sendResponseHeaders(400, 0);
                    return;
                }
            }

            CommonConstants.LOGGER.info("Sending file: " + path);
            exchange.sendResponseHeaders(200, Files.size(path));
            Files.copy(path, exchange.getResponseBody());
        }));
        httpServer.createContext("/assets/img/", (httpExchange) -> {
            OutputStream responseBody = new BufferedOutputStream(httpExchange.getResponseBody(), 8192);
            String path = httpExchange.getRequestURI().getPath();
            if (!path.startsWith("/assets/img/") || path.contains("..")) {
                CommonConstants.LOGGER.error("Invalid path: " + path);
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }
            try (InputStream resource = ServerHttpSite.class.getResourceAsStream("/html" + path)) {
                if (resource == null) {
                    CommonConstants.LOGGER.error("Resource not found: " + path);
                    httpExchange.sendResponseHeaders(404, 0);
                    responseBody.close();
                    return;
                }
                httpExchange.getResponseHeaders().add("Content-Type", "image/png");
                httpExchange.sendResponseHeaders(200, 0);

                resource.transferTo(responseBody);

                responseBody.close();
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to send asset", e);
                responseBody.close();
            }
        });
        httpServer.createContext("/assets/", (httpExchange) -> {
            OutputStream responseBody = new BufferedOutputStream(httpExchange.getResponseBody(), 8192);
            String path = httpExchange.getRequestURI().getPath();
            if (!path.startsWith("/assets/") || path.contains("..")) {
                CommonConstants.LOGGER.error("Invalid path: " + path);
                httpExchange.sendResponseHeaders(400, 0);
                return;
            }
            try (InputStream resource = ServerHttpSite.class.getResourceAsStream("/html" + path)) {
                if (resource == null) {
                    CommonConstants.LOGGER.error("Resource not found: " + path);
                    httpExchange.sendResponseHeaders(404, 0);
                    responseBody.close();
                    return;
                }

                if (path.endsWith(".css")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/css");
                } else if (path.endsWith(".js")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/javascript");
                } else if (path.endsWith(".mjs")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "application/javascript+module");
                } else if (path.endsWith(".otf")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "font/otf");
                } else if (path.endsWith(".ttf")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "font/ttf");
                } else if (path.endsWith(".woff2")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "font/woff2");
                } else if (path.endsWith(".woff")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "font/woff");
                } else if (path.endsWith(".png")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "image/png");
                } else if (path.endsWith(".ico")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "image/x-icon");
                } else if (path.endsWith(".svg")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "image/svg+xml");
                } else if (path.endsWith(".html")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/html");
                } else if (path.endsWith(".json")) {
                    httpExchange.getResponseHeaders().add("Content-Type", "application/json");
                } else {
                    httpExchange.getResponseHeaders().add("Content-Type", "text/plain");
                }
                httpExchange.getResponseHeaders().add("Content-Type", "image/png");
                httpExchange.sendResponseHeaders(200, 0);

                if (resource != null) {
                    resource.transferTo(responseBody);
                }

                responseBody.close();
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to send asset", e);
                responseBody.close();
            }
        });
        httpServer.createContext("/favicon.ico", (httpExchange) -> {
            httpExchange.sendResponseHeaders(200, 0);
            OutputStream responseBody = httpExchange.getResponseBody();
            try (InputStream favicon = getClass().getClassLoader().getResourceAsStream("html/favicon.ico")) {
                if (favicon != null) {
                    favicon.transferTo(responseBody);
                }

                responseBody.close();
            } catch (NullPointerException | IOException e) {
                CommonConstants.LOGGER.error("Failed to send favicon", e);
            }
        });
        httpServer.createContext("/login", exchange -> {
            // Get username and password from the query
            String query = exchange.getRequestURI().getQuery();
            try {
                if (query != null && !query.isEmpty()) {
                    if (isAuthenticated(exchange, false)) {
                        exchange.getResponseHeaders().add("Location", "/dashboard");
                        exchange.sendResponseHeaders(HttpCodes.FOUND, 0);
                        exchange.close();
                        return;
                    }
                    if (!login(query)) {
                        CommonConstants.LOGGER.error("Failed to login: " + query);
                        exchange.getResponseHeaders().add("Location", "/login");
                    } else {
                        exchange.getResponseHeaders().add("Set-Cookie", "token=" + apiHandler.generateToken() + "; Path=; SameSite=Strict; Secure");
                        exchange.getResponseHeaders().add("Location", "/dashboard");
                    }
                    exchange.sendResponseHeaders(HttpCodes.FOUND, 0);
                    exchange.close();
                    return;
                }
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to send login page", e);
                exchange.sendResponseHeaders(HttpCodes.INTERNAL_SERVER_ERROR, 0);
                String message = e.getMessage();
                exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
                exchange.close();
                return;
            }

            try (InputStream loginPage = getClass().getClassLoader().getResourceAsStream("html/login.html")) {
                if (loginPage == null) {
                    CommonConstants.LOGGER.error("Failed to load login page");
                    exchange.sendResponseHeaders(HttpCodes.INTERNAL_SERVER_ERROR, 0);
                    exchange.getResponseBody().write("Failed to load login page".getBytes(StandardCharsets.UTF_8));
                    exchange.close();
                    return;
                }

                exchange.sendResponseHeaders(HttpCodes.OK, 0);
                loginPage.transferTo(exchange.getResponseBody());
                exchange.close();
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to send login page", e);
                exchange.sendResponseHeaders(HttpCodes.INTERNAL_SERVER_ERROR, 0);
                String message = e.getMessage();
                exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
                exchange.close();
            }
        });
        httpServer.createContext("/dashboard", new PasswordProtectedHandler(new DashboardPageHandler("home")));
        httpServer.createContext("/tools", new PasswordProtectedHandler(new DashboardPageHandler("tools")));
        httpServer.createContext("/stats", new PasswordProtectedHandler(new DashboardPageHandler("stats")));
        httpServer.createContext("/console", new PasswordProtectedHandler(new DashboardPageHandler("console")));
        httpServer.createContext("/settings", new PasswordProtectedHandler(new DashboardPageHandler("settings")));
        httpServer.createContext("/profile", new PasswordProtectedHandler(new DashboardPageHandler("profile")));
        httpServer.createContext("/404", new PasswordProtectedHandler(new DashboardPageHandler("404")));

        httpServer.createContext("/logout", (httpExchange) -> {
            // Logout
            httpExchange.getResponseHeaders().add("Set-Cookie", "token=; Path=/; SameSite=Strict; Secure");
            httpExchange.getResponseHeaders().add("Location", "/");
            httpExchange.sendResponseHeaders(302, 0);
            httpExchange.close();
        });
        httpServer.createContext("/", (httpExchange) -> {
            if (!isAuthenticated(httpExchange, false)) {
                if (!httpExchange.getRequestURI().getPath().equals("/")) {
                    send404(httpExchange);
                    return;
                }
                redirectToLogin(httpExchange);
            } else {
                if (!httpExchange.getRequestURI().getPath().equals("/")) {
                    sendMainDashboard(httpExchange, "/404");
                    httpExchange.sendResponseHeaders(302, 0);
                    httpExchange.close();
                    return;
                }
                // Redirect to dashboard
                httpExchange.getResponseHeaders().add("Location", "/dashboard");
                httpExchange.sendResponseHeaders(302, 0);
                httpExchange.close();
            }
        });
    }

    private void send404(HttpExchange exchange) throws IOException {
        CommonConstants.LOGGER.info("Sending 404 to: " + exchange.getRemoteAddress());
        exchange.sendResponseHeaders(404, 0);
        try (InputStream notFound = getClass().getClassLoader().getResourceAsStream("html/404.html")) {
            if (notFound != null) {
                notFound.transferTo(exchange.getResponseBody());
            }
        }
        exchange.close();
    }

    private void sendMainDashboard(HttpExchange exchange, String page) throws IOException {
        try (InputStream dashboard = getClass().getClassLoader().getResourceAsStream("html/dashboard.html")) {
            exchange.sendResponseHeaders(200, 0);
            if (dashboard != null) {
                byte[] bytes = dashboard.readAllBytes();
                String html = new String(bytes, StandardCharsets.UTF_8)
                        .replace("%%HOME_ACTIVE%%", page.equals("home") ? " active" : " text-white")
                        .replace("%%TOOLS_ACTIVE%%", page.equals("tools") ? " active" : " text-white")
                        .replace("%%STATS_ACTIVE%%", page.equals("stats") ? " active" : " text-white")
                        .replace("%%CONSOLE_ACTIVE%%", page.equals("console") ? " active" : " text-white")
                        .replace("%%SETTINGS_ACTIVE%%", page.equals("settings") ? " active" : " text-white")
                        .replace("%%PROFILE_ACTIVE%%", page.equals("profile") ? " active" : " text-white");

                String pageHtml;
                try (InputStream pageStream = getClass().getClassLoader().getResourceAsStream("html/dashboard/" + page + ".html")) {
                    if (pageStream == null) {
                        pageHtml = "";
                        html = html.replace("%%CONTENT%%", pageHtml)
                        ;
                        exchange.getResponseBody().write(html.getBytes(StandardCharsets.UTF_8));
                        exchange.close();
                        return;
                    }
                    pageHtml = new String(pageStream.readAllBytes(), StandardCharsets.UTF_8)
                            .replace("%%SERVER_NAME%%", serverName)
                            .replace("%%SERVER_VERSION%%", serverVersion)
                            .replace("%%SERVER_IP%%", QuantumServer.get() != null ? (ServerConfig.hostname + ":" + ServerConfig.port) : "???")
                            .replace("%%SERVER_STATUS%%", QuantumServer.get() != null ? "Online" : "Offline")
                            .replace("%%SERVER_STATUS_COLOR%%", QuantumServer.get() != null ? "green" : "red")
                            .replace("%%SERVER_PLAYERS%%", QuantumServer.get() != null ? QuantumServer.get().getPlayerCount() + " / " + QuantumServer.get().getMaxPlayers() : "???");
                } catch (Exception e) {
                    pageHtml = "";
                }

                html = html.replace("%%CONTENT%%", pageHtml);
                exchange.getResponseBody().write(html.getBytes(StandardCharsets.UTF_8));
            }

            exchange.close();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to send dashboard", e);
            exchange.sendResponseHeaders(HttpCodes.INTERNAL_SERVER_ERROR, 0);
            String message = e.getMessage();
            exchange.getResponseBody().write(message.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        }
    }

    private void redirectToLogin(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", "/login");
        exchange.sendResponseHeaders(302, 0);
    }

    private void prepare() {
        if (Files.notExists(Path.of("password.txt"))) {
            try (FileWriter writer = new FileWriter("password.txt")) {
                String initialPassword = generatePassword();
                CommonConstants.LOGGER.info("Generated initial password: " + initialPassword);
                writer.write("admin:" + hash(initialPassword));
            } catch (IOException e) {
                CommonConstants.LOGGER.error("Failed to create password.txt", e);
            }
        }
    }

    private String hash(String password) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-512");
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private String generatePassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes).replace("=", "").replace("/", "").replace("+", "");
    }

    private boolean login(String query) {
        String[] parts = query.split("&");

        if (parts.length != 2) return false;
        if (!parts[0].startsWith("username=") || !parts[1].startsWith("password=")) return false;
        String username = parts[0].split("=")[1];
        String password = parts[1].split("=")[1];
        boolean login = login(username, hash(password));
        if (!login) CommonConstants.LOGGER.error("Failed to login: " + username);
        return login;
    }

    public static ServerHttpSite getInstance() {
        return instance;
    }

    public static boolean isAuthenticated(HttpExchange httpExchange) throws IOException {
        return isAuthenticated(httpExchange, true);
    }

    public static boolean isAuthenticated(HttpExchange httpExchange, boolean kick) throws IOException {
        // Check for token cookie
        String cookie = httpExchange.getRequestHeaders().getFirst("Cookie");
        if (cookie != null && cookie.contains("token=")) {
            String token = cookie.split("token=")[1].split(";")[0];
            boolean tokenValid = apiHandler.isTokenValid(token);
            if (!tokenValid && kick) {
                httpExchange.getResponseHeaders().add("Message", "Invalid token");
                respondUnauthorized(httpExchange);
                httpExchange.close();
            }
            return tokenValid;
        }

        String auth = httpExchange.getRequestHeaders().getFirst("Authorization");
        if ((auth == null || !auth.startsWith("Basic ")) && kick) {
            CommonConstants.LOGGER.info("Missing credentials");
            httpExchange.getResponseHeaders().add("Message", "Missing credentials");
            respondUnauthorized(httpExchange);
            httpExchange.close();
            return false;
        } else if (auth == null || !auth.startsWith("Basic ")) {
            CommonConstants.LOGGER.info("Missing credentials");
            return false;
        }

        String[] parts = new String(Base64.getDecoder().decode(auth.substring(6))).split(":", 2);
        if (parts.length != 2 && kick) {
            CommonConstants.LOGGER.info("Invalid credentials: " + parts[0]);
            httpExchange.getResponseHeaders().add("Message", "Invalid credentials");
            respondUnauthorized(httpExchange);
            httpExchange.close();
            return false;
        } else if (parts.length != 2) {
            CommonConstants.LOGGER.info("Invalid credentials: " + parts[0]);
            return false;
        }

        boolean login = login(parts[0], parts[1]);
        if (!login && kick) {
            CommonConstants.LOGGER.error("Failed to login: " + parts[0]);
            httpExchange.getResponseHeaders().add("Message", "Invalid credentials");
            respondUnauthorized(httpExchange);
            httpExchange.close();
        } else if (!login) {
            CommonConstants.LOGGER.info("Failed to login: " + parts[0]);
            return false;
        }
        return login;
    }

    protected static boolean login(String username, String password) {
        String passwordInFile = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("./password.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(":")[0].equals(username)) {
                    passwordInFile = line.split(":")[1];
                    break;
                }
            }
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to read password.txt", e);
        }

        boolean equals = password.equals(passwordInFile);
        if (!equals) CommonConstants.LOGGER.error("Failed to login: " + username);
        else CommonConstants.LOGGER.info("Logged in: " + username);
        return equals;
    }

    @Override
    public void close() throws IOException {
        HttpServer httpServer = HttpServer.create();
        httpServer.stop(0);
    }

    private static class Sha512 {
        public static boolean verify(String hash, String passwordInFile) {
            MessageDigest sha512 = null;
            try {
                sha512 = MessageDigest.getInstance("SHA-512");
            } catch (Exception e) {
                CommonConstants.LOGGER.error("Failed to get SHA-512", e);
            }
            return sha512 != null && Arrays.equals(sha512.digest(passwordInFile.getBytes(StandardCharsets.UTF_8)), hash.getBytes(StandardCharsets.UTF_8));
        }
    }

    private record PasswordProtectedHandler(HttpHandler handler) implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            if (isAuthenticated(httpExchange)) {
                handler.handle(httpExchange);
            } else {
                respondUnauthorized(httpExchange);
            }
        }

    }

    private static void respondUnauthorized(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add("Location", "/login");
        httpExchange.sendResponseHeaders(HttpCodes.FOUND, 0);
    }

    public static void main(String[] args) {
        new ServerPlatform();
        try {
            instance = new ServerHttpSite();
        } catch (IOException e) {
            CommonConstants.LOGGER.error("Failed to start server", e);
        }
    }

    private class DashboardPageHandler implements HttpHandler {
        private final String path;

        public DashboardPageHandler(String path) {
            this.path = path;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // Show dashboard
            if (!isAuthenticated(exchange, false)) {
                ServerHttpSite.this.redirectToLogin(exchange);
                return;
            }
            ServerHttpSite.this.sendMainDashboard(exchange, path);
        }
    }
}
