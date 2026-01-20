package com.saunhardy.presenceapi;

import com.google.gson.JsonObject;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ApiClient {
    private HttpClient httpClient;
    private final ExecutorService executor;
    private volatile boolean shuttingDown = false;

    public ApiClient() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "PresenceAPI-Worker");
            t.setDaemon(false); // Changed to false so thread stays alive during shutdown
            return t;
        });
    }

    private HttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Config.TIMEOUT_SECONDS.get()))
                    .build();
        }
        return httpClient;
    }

    public void sendPresenceData(PlayerPresenceData data) {
        if (!Config.ENABLED.get() || shuttingDown) {
            return;
        }

        executor.submit(() -> {
            try {
                sendWithRetry(data, 0);
            } catch (Exception e) {
                if (Config.LOG_ERRORS.get()) {
                    presenceAPI.LOGGER.error("Failed to send presence data after all retries", e);
                }
            }
        });
    }

    private void sendWithRetry(PlayerPresenceData data, int attempt) {
        try {
            String token = generateJWT();
            String jsonBody = data.toJson().toString();

            if (Config.LOG_REQUESTS.get()) {
                presenceAPI.LOGGER.info("Sending presence data: {}", jsonBody);
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(Config.API_ENDPOINT.get()))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .timeout(Duration.ofSeconds(Config.TIMEOUT_SECONDS.get()))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (Config.LOG_RESPONSES.get()) {
                presenceAPI.LOGGER.info("API Response [{}]: {}", response.statusCode(), response.body());
            }

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (Config.LOG_REQUESTS.get()) {
                    presenceAPI.LOGGER.info("Successfully sent presence data for player {}",
                            data.toJson().get("minecraftUsername").getAsString());
                }
            } else {
                throw new RuntimeException("API returned status code: " + response.statusCode());
            }
        } catch (Exception e) {
            if (Config.RETRY_ON_FAILURE.get() && attempt < Config.MAX_RETRIES.get() && !shuttingDown) {
                if (Config.LOG_ERRORS.get()) {
                    presenceAPI.LOGGER.warn("Failed to send presence data (attempt {}/{}): {}",
                            attempt + 1, Config.MAX_RETRIES.get(), e.getMessage());
                }
                try {
                    Thread.sleep(1000L * (attempt + 1));
                    sendWithRetry(data, attempt + 1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } else {
                throw new RuntimeException("Failed to send presence data", e);
            }
        }
    }

    private String generateJWT() {
        try {
            String secret = Config.JWT_SECRET.get();

            JsonObject header = new JsonObject();
            header.addProperty("alg", "HS256");
            header.addProperty("typ", "JWT");

            JsonObject payload = new JsonObject();
            long nowSeconds = System.currentTimeMillis() / 1000;
            payload.addProperty("iat", nowSeconds);
            payload.addProperty("exp", nowSeconds + 60);

            String encodedHeader = base64UrlEncode(header.toString());
            String encodedPayload = base64UrlEncode(payload.toString());

            String dataToSign = encodedHeader + "." + encodedPayload;
            String signature = hmacSha256(dataToSign, secret);

            return dataToSign + "." + signature;
        } catch (Exception e) {
            presenceAPI.LOGGER.error("Failed to generate JWT", e);
            throw new RuntimeException("JWT generation failed", e);
        }
    }

    private String base64UrlEncode(String input) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    private String hmacSha256(String data, String secret) throws Exception {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        sha256Hmac.init(secretKey);
        byte[] hash = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
    }

    public void shutdown() {
        presenceAPI.LOGGER.info("Shutting down PresenceAPI gracefully...");
        shuttingDown = true;

        executor.shutdown();

        try {
            // Wait up to 10 seconds for pending tasks to complete
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                presenceAPI.LOGGER.warn("Executor did not terminate in time, forcing shutdown");
                executor.shutdownNow();

                // Wait a bit more for forced shutdown
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    presenceAPI.LOGGER.error("Executor did not terminate after forced shutdown");
                }
            } else {
                presenceAPI.LOGGER.info("PresenceAPI shutdown completed successfully");
            }
        } catch (InterruptedException e) {
            presenceAPI.LOGGER.error("Shutdown interrupted", e);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}