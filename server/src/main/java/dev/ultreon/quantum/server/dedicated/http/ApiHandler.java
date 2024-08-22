package dev.ultreon.quantum.server.dedicated.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.ultreon.quantum.CommonConstants;
import dev.ultreon.quantum.api.commands.CommandSpecValues;
import net.fabricmc.loader.api.FabricLoader;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static dev.ultreon.quantum.server.dedicated.http.ServerHttpSite.isAuthenticated;
import static dev.ultreon.quantum.server.dedicated.http.ServerHttpSite.login;

public class ApiHandler implements HttpHandler {
    private final Set<String> tokens = new CopyOnWriteArraySet<>();

    public boolean isTokenValid(String token) {
        return tokens.contains(token);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestURI().getPath().startsWith("/api/")) {
            exchange.sendResponseHeaders(404, 0);
            return;
        }

        try {
            String apiUrl = exchange.getRequestURI().getPath().substring(5);
            String[] args = apiUrl.split("/");

            switch (args[0]) {
                case "ping" -> {
                    if (args.length == 1) {
                        exchange.getResponseBody().write("pong".getBytes());
                        exchange.sendResponseHeaders(200, 0);
                        exchange.close();
                        return;
                    }
                }
                case "version" -> {
                    if (args.length == 1) {
                        exchange.getResponseBody().write(FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getMetadata().getVersion().getFriendlyString().getBytes());
                        exchange.sendResponseHeaders(200, 0);
                        exchange.close();
                        return;
                    }
                }
                case "login" -> {
                    if (evalLoginApi(exchange)) return;
                }
                case "logout" -> {
                    if (evalLogoutApi(exchange)) return;
                }
                case "console-logs" -> {
                    if (!isAuthenticated(exchange)) return;
                    if (args.length == 1) {
                        Path path = Paths.get("logs/debug.log");
                        if (Files.notExists(path)) {
                            exchange.sendResponseHeaders(404, 0);
                            return;
                        }
                        exchange.sendResponseHeaders(200, 0);
                        try (var is = Files.newInputStream(path, StandardOpenOption.READ)) {
                            is.transferTo(exchange.getResponseBody());
                        }
                        exchange.close();
                        return;
                    }
                }
                default -> {
                    exchange.sendResponseHeaders(404, 0);
                    return;
                }
            }
        } catch (Throwable e) {
            CommonConstants.LOGGER.error("Failed to handle API request", e);
            exchange.sendResponseHeaders(500, 0);
            return;
        }

        exchange.sendResponseHeaders(404, 0);
    }

    private boolean evalLogoutApi(HttpExchange exchange) throws IOException {
        if (isAuthenticated(exchange)) {
            // Logout
            for (String cookie : exchange.getRequestHeaders().get("Cookie")) {
                if (cookie.contains("token=")) {
                    tokens.remove(cookie.indexOf("token=") + 6);
                    exchange.sendResponseHeaders(200, 0);
                    return true;
                } else {
                    exchange.sendResponseHeaders(404, 0);
                    return true;
                }
            }
            exchange.sendResponseHeaders(300, 0);
            return true;
        }
        return false;
    }

    private boolean evalLoginApi(HttpExchange exchange) throws IOException {
        if (isAuthenticated(exchange)) {
            exchange.sendResponseHeaders(404, 0);
            return true;
        }

        try {
            JsonObject jsonObject = CommonConstants.GSON.fromJson(new InputStreamReader(exchange.getRequestBody()), JsonObject.class);
            String username = jsonObject.get("username").getAsString();
            String password = jsonObject.get("password").getAsString();

            String s = generateToken();
            if (s == null) {
                exchange.getResponseBody().write(json("""
                        {
                            "success": false,
                            "message": "Failed to generate token"
                        }
                        """));
                exchange.getResponseHeaders().add("Cookie", "token=" + s + "; Path=/; SameSite=Strict; Secure");
                exchange.sendResponseHeaders(500, 0);
                return true;
            }
            boolean loggedIn = login(username, password);
            exchange.sendResponseHeaders(loggedIn ? 200 : 401, 0);

            if (loggedIn) {
                exchange.getResponseBody().write(json("{\n" +
                                                      "    \"success\": true,\n" +
                                                      "    \"token\": \"" + s + "\"\n" +
                                                      "}\n"));
            }
        } catch (JsonParseException e) {
            exchange.sendResponseHeaders(HttpCodes.JSON_DECODE_ERROR, 0);
            return true;
        } catch (Exception e) {
            exchange.sendResponseHeaders(HttpCodes.BAD_REQUEST, 0);
            return true;
        }
        return false;
    }

    String generateToken() {
        MessageDigest sha512 = null;
        try {
            sha512 = MessageDigest.getInstance("SHA-512");
            sha512.update(SecureRandom.getSeed(64));
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to get SHA-512", e);
        }
        if (sha512 != null) {
            String s = Base64.getEncoder().encodeToString(sha512.digest(SecureRandom.getSeed(64)));
            this.tokens.add(s);
            return s;
        }

        return null;
    }

    private byte[] json(@Language("JSON") String json) {
        return json.getBytes();
    }
}
