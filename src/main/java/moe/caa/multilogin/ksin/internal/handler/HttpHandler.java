package moe.caa.multilogin.ksin.internal.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.ksin.internal.main.Ksin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpHandler {
    private static final String HTTP_USER_AGENT = "Ksin/1.0";
    private HttpClient httpClient;

    public void rebuildHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder();
        Ksin.INSTANCE.config.proxy.get().attachProxy(builder)
                .executor(Ksin.INSTANCE.asyncExecutor)
                .connectTimeout(Duration.of(Ksin.INSTANCE.config.httpTimeout.get(), ChronoUnit.MILLIS));
        httpClient = builder.build();
    }

    public HttpRequest buildGetTextureRequest(String textureUrl) {
        return HttpRequest.newBuilder()
                .uri(URI.create(textureUrl))
                .header("User-Agent", HTTP_USER_AGENT)
                .GET()
                .build();
    }

    public CompletableFuture<List<String>> getSupportedCapes() {
        return sendAsyncRetry(HttpRequest.newBuilder()
                        .uri(URI.create(Ksin.INSTANCE.config.mineSkin.get().apiRoot.get() + "/v2/capes"))
                        .header("Accept", "application/json")
                        .header("User-Agent", HTTP_USER_AGENT)
                        .header("Authorization", "Bearer " + Ksin.INSTANCE.config.mineSkin.get().apiKey.get())
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString(),
                Ksin.INSTANCE.config.httpRetries.get()).thenApply(stringHttpResponse -> {
            List<String> result = new ArrayList<>();
            for (JsonElement element : JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject().getAsJsonArray("capes")) {
                JsonObject subJsonObject = element.getAsJsonObject();
                JsonElement supportedElement = subJsonObject.get("supported");
                if (supportedElement != null && supportedElement.isJsonPrimitive() && supportedElement.getAsBoolean()) {
                    result.add(subJsonObject.getAsJsonPrimitive("alias").getAsString());
                }
            }
            return result;
        });
    }

    public <T> CompletableFuture<HttpResponse<T>> sendAsyncRetry(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, int retryCount) {
        return sendAsync(request, responseBodyHandler).handle((response, throwable) -> {
            if (throwable != null) {
                if (retryCount > 0) {
                    Ksin.INSTANCE.logger.debug("Retrying HTTP request due to failure: " + request.uri() + ", remaining retries: " + (retryCount - 1));
                    return sendAsyncRetry(request, responseBodyHandler, retryCount - 1);
                } else {
                    CompletableFuture<HttpResponse<T>> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(throwable);
                    return failedFuture;
                }
            } else {
                return CompletableFuture.completedFuture(response);
            }
        }).thenCompose(future -> future);
    }

    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
        Ksin.INSTANCE.asyncExecutor.execute(() -> {
            Ksin.INSTANCE.logger.debug("Sending HTTP request(" + request.method() + "): " + request.uri());
            long currentTimeMills = System.currentTimeMillis();
            try {
                HttpResponse<T> response = httpClient.send(request, responseBodyHandler);
                Ksin.INSTANCE.logger.debug("Received HTTP response(" + response.statusCode() + "): " + request.uri() + ", took " + (System.currentTimeMillis() - currentTimeMills) + "ms");
                future.complete(response);
            } catch (Throwable e) {
                Ksin.INSTANCE.logger.debug("HTTP request failed: " + request.uri() + ", took " + (System.currentTimeMillis() - currentTimeMills) + "ms", e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
