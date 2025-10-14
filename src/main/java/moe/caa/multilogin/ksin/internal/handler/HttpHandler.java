package moe.caa.multilogin.ksin.internal.handler;

import moe.caa.multilogin.ksin.internal.main.Ksin;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;

public sealed class HttpHandler permits MineSkinHttpHandler {
    protected static final String HTTP_USER_AGENT = "Ksin/1.0";
    protected HttpClient httpClient;

    public void rebuildHttpClient() {
        HttpClient.Builder builder = HttpClient.newBuilder();
        httpClient = Ksin.INSTANCE.config.proxy.get().attachProxy(builder)
                .executor(Ksin.INSTANCE.asyncExecutor)
                .connectTimeout(Duration.of(Ksin.INSTANCE.config.httpTimeout.get(), ChronoUnit.MILLIS))
                .build();
    }

    protected <T> CompletableFuture<HttpResponse<T>> sendAsyncRetry(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, int retryCount) {
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

    protected <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
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
