package moe.caa.multilogin.ksin.internal.handler;

import moe.caa.multilogin.ksin.internal.main.Ksin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public class GameProfileRequestSkinRepairer {
    private static final String HTTP_USER_AGENT = "Ksin/1.0";
    private HttpClient httpClient;

    public void rebuildHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder();
        Ksin.INSTANCE.config.proxy.get().attachProxy(builder);
        builder.executor(Ksin.INSTANCE.asyncExecutor);
        builder.connectTimeout(Duration.of(Ksin.INSTANCE.config.httpTimeout.get(), ChronoUnit.MILLIS));
        httpClient = builder.build();
    }

    private HttpRequest buildGetTextureHttpRequest(String textureUrl) {
        return HttpRequest.newBuilder()
                .uri(URI.create(textureUrl))
                .header("User-Agent", HTTP_USER_AGENT)
                .GET()
                .build();
    }

    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        CompletableFuture<HttpResponse<T>> future = new CompletableFuture<>();
        Ksin.INSTANCE.asyncExecutor.execute(() -> {

            // todo log
            try {
                HttpResponse<T> response = httpClient.send(request, responseBodyHandler);
                future.complete(response);
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }
}
