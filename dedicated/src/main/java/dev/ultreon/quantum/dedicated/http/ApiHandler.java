package dev.ultreon.quantum.dedicated.http;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dev.ultreon.quantum.CommonConstants;
import net.fabricmc.loader.api.FabricLoader;
import org.intellij.lang.annotations.Language;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static dev.ultreon.quantum.dedicated.http.ServerHttpSite.isAuthenticated;
import static dev.ultreon.quantum.dedicated.http.ServerHttpSite.login;

public class ApiHandler implements HttpHandler {
    public static final String TOKEN_KEY = "token=";
    private final Set<String> tokens = new CopyOnWriteArraySet<>();

    public boolean isTokenValid(String token) {
        return tokens.contains(token);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestURI().getPath().startsWith("/api/")) {
            exchange.sendResponseHeaders(HttpCodes.NOT_FOUND, 0);
            return;
        }

        try {
            if (doApi(exchange)) return;
        } catch (Exception e) {
            CommonConstants.LOGGER.error("Failed to handle API request", e);
            exchange.sendResponseHeaders(500, 0);
            return;
        }

        exchange.sendResponseHeaders(HttpCodes.NOT_FOUND, 0);
    }

    private boolean doApi(HttpExchange exchange) throws IOException {
        String apiUrl = exchange.getRequestURI().getPath().substring(5);
        String[] args = apiUrl.split("/");

        switch (args[0]) {
            case "ping":
                if (ping(exchange, args)) return true;
                break;
            case "version":
                if (version(exchange, args)) return true;
                break;
            case "login":
                if (evalLoginApi(exchange)) return true;
                break;
            case "logout":
                if (evalLogoutApi(exchange)) return true;
                break;
            case "console-logs":
                if (consoleLogs(exchange, args)) return true;
                break;
            default:
                exchange.sendResponseHeaders(HttpCodes.NOT_FOUND, 0);
                return true;
        }
        return false;
    }

    private static boolean consoleLogs(HttpExchange exchange, String[] args) throws IOException {
        if (!isAuthenticated(exchange)) return true;
        if (args.length == 1) {
            FileHandle path = Gdx.files.local("logs/debug.log");
            if (!path.exists()) {
                exchange.sendResponseHeaders(HttpCodes.NOT_FOUND, 0);
                return true;
            }
            exchange.sendResponseHeaders(HttpCodes.OK, 0);
            try (var is = path.read()) {
                is.transferTo(exchange.getResponseBody());
            }
            exchange.close();
            return true;
        }
        return false;
    }

    private static boolean version(HttpExchange exchange, String[] args) throws IOException {
        if (args.length == 1) {
            exchange.getResponseBody().write(FabricLoader.getInstance().getModContainer(CommonConstants.NAMESPACE).orElseThrow().getMetadata().getVersion().getFriendlyString().getBytes());
            exchange.sendResponseHeaders(HttpCodes.OK, 0);
            exchange.close();
            return true;
        }
        return false;
    }

    private static boolean ping(HttpExchange exchange, String[] args) throws IOException {
        if (args.length == 1) {
            exchange.getResponseBody().write("pong".getBytes());
            exchange.sendResponseHeaders(HttpCodes.OK, 0);
            exchange.close();
            return true;
        }
        return false;
    }

    private boolean evalLogoutApi(HttpExchange exchange) throws IOException {
        if (isAuthenticated(exchange)) {
            // Logout
            for (String cookie : exchange.getRequestHeaders().get("Cookie")) {
                if (cookie.contains(TOKEN_KEY)) {
                    int i = cookie.indexOf(TOKEN_KEY) + 6;
                    tokens.remove(cookie.substring(i, cookie.indexOf(";", i)));
                    exchange.sendResponseHeaders(HttpCodes.OK, 0);
                    return true;
                }
            }
            exchange.sendResponseHeaders(HttpCodes.TEMPORARY_REDIRECT, 0);
            return true;
        }
        return false;
    }

    private boolean evalLoginApi(HttpExchange exchange) throws IOException {
        if (isAuthenticated(exchange)) {
            exchange.sendResponseHeaders(HttpCodes.NOT_FOUND, 0);
            return true;
        }

        try {
            JsonValue jsonObject = new JsonReader().parse(exchange.getRequestBody());
            String username = jsonObject.get("username").asString();
            String password = jsonObject.get("password").asString();

            String s = generateToken();
            if (s == null) {
                exchange.getResponseBody().write(json("{\n" +
                                                      "    \"success\": false,\n" +
                                                      "    \"message\": \"Failed to generate token\"\n" +
                                                      "}\n"));
                exchange.getResponseHeaders().getOrDefault("Cookie", new ArrayList<>()).remove(TOKEN_KEY + null + "; Path=/; SameSite=Strict; Secure");
                exchange.sendResponseHeaders(500, 0);
                return true;
            }
            boolean loggedIn = login(username, password);
            exchange.sendResponseHeaders(loggedIn ? HttpCodes.OK : 401, 0);

            if (loggedIn) {
                exchange.getResponseBody().write(json("{\n" +
                                                      "    \"success\": true,\n" +
                                                      "    \"token\": \"" + s + "\"\n" +
                                                      "}\n"));
            }
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
