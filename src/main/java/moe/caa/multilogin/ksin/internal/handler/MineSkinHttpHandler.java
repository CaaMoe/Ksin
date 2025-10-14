package moe.caa.multilogin.ksin.internal.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.ksin.internal.main.Ksin;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public non-sealed class MineSkinHttpHandler extends HttpHandler {
    public CompletableFuture<InputStream> getTextureFromUrl(String textureUrl) {
        return sendAsyncRetry(HttpRequest.newBuilder()
                .uri(URI.create(textureUrl))
                .header("User-Agent", HTTP_USER_AGENT)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofInputStream(), Ksin.INSTANCE.config.httpRetries.get())
                .thenApply(HttpResponse::body);
    }

    public CompletableFuture<List<SupportCape>> getSupportedCapes() {
        return sendAsyncRetry(HttpRequest.newBuilder()
                        .uri(URI.create(Ksin.INSTANCE.config.mineSkin.get().apiRoot.get() + "/v2/capes"))
                        .header("Accept", "application/json")
                        .header("User-Agent", HTTP_USER_AGENT)
                        .header("Authorization", "Bearer " + Ksin.INSTANCE.config.mineSkin.get().apiKey.get())
                        .GET()
                        .build(), HttpResponse.BodyHandlers.ofString(),
                Ksin.INSTANCE.config.httpRetries.get()).thenApply(stringHttpResponse -> {
            List<SupportCape> result = new ArrayList<>();
            for (JsonElement element : JsonParser.parseString(stringHttpResponse.body()).getAsJsonObject().getAsJsonArray("capes")) {
                JsonObject subJsonObject = element.getAsJsonObject();
                JsonElement supportedElement = subJsonObject.get("supported");
                if (supportedElement != null && supportedElement.isJsonPrimitive() && supportedElement.getAsBoolean()) {
                    result.add(new SupportCape(
                            UUID.fromString(subJsonObject.getAsJsonPrimitive("uuid").getAsString()),
                            subJsonObject.getAsJsonPrimitive("alias").getAsString()
                    ));
                }
            }
            return result;
        });
    }

    public enum SkinVariant {
        CLASSIC,
        SLIM;
    }

    public enum SkinVisibility {
        PUBLIC,
        UNLISTED,
        PRIVATE;
    }

    public record SupportCape(
            UUID uuid,
            String alias
    ) {
    }
}
