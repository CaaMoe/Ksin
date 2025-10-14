package moe.caa.multilogin.ksin.internal.handler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.ksin.internal.main.Ksin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public non-sealed class MineSkinHttpHandler extends HttpHandler {
    private CompletableFuture<BufferedImage> getTextureImage(String textureUrl) {
        return sendAsyncRetry(HttpRequest.newBuilder()
                .uri(URI.create(textureUrl))
                .header("User-Agent", HTTP_USER_AGENT)
                .GET()
                .build(), HttpResponse.BodyHandlers.ofInputStream(), Ksin.INSTANCE.config.httpRetries.get())
                .thenApply(response -> {
                    try (InputStream stream = response.body()) {
                        return ImageIO.read(stream);
                    } catch (IOException exception) {
                        throw new RuntimeException(exception);
                    }
                });
    }

    private HttpRequest.Builder addMineSkinAuthHeader(HttpRequest.Builder builder) {
        String apiKey = Ksin.INSTANCE.config.mineSkin.get().apiKey.get();
        if (apiKey.isEmpty()) return builder;
        return builder.header("Authorization", "Bearer " + apiKey);
    }

    private <T extends MineSkinResponse> CompletableFuture<T> fetch(HttpRequest.Builder builder, Function<JsonElement, T> parser) {
        return sendAsyncRetry(addMineSkinAuthHeader(builder)
                        .header("Accept", "application/json")
                        .header("User-Agent", HTTP_USER_AGENT)
                        .build(), HttpResponse.BodyHandlers.ofString(),
                Ksin.INSTANCE.config.httpRetries.get())
                .thenApply(stringHttpResponse -> JsonParser.parseString(stringHttpResponse.body()))
                .thenApply(jsonElement -> {
                    try {
                        T response = parser.apply(jsonElement);
                        if (!response.messages.isEmpty() || !response.errors.isEmpty() || !response.warnings.isEmpty()) {
                            Ksin.INSTANCE.logger.debug("MineSkin api response: messages=" + response.messages + ", warnings=" + response.warnings + ", errors=" + response.errors);
                        }
                        return response;
                    } catch (Throwable e) {
                        Ksin.INSTANCE.logger.debug("Failed to parse mineskin response: " + jsonElement, e);
                        throw e;
                    }
                });
    }

    public @NotNull CompletableFuture<MineSkinResponse.@NotNull CapesResponse> getCapes() {
        return fetch(HttpRequest.newBuilder()
                        .uri(URI.create(Ksin.INSTANCE.config.mineSkin.get().apiRoot.get() + "v2/capes"))
                , jsonElement -> MineSkinResponse.CapesResponse.parse(jsonElement.getAsJsonObject()));
    }

    private String toBase64EncodedImages(@NotNull RenderedImage image) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }

    public @NotNull CompletableFuture<MineSkinResponse.GenerateResponse> generate(@NotNull RenderedImage textureImage, @Nullable Cape cape, @NotNull SkinVariant variant, @NotNull SkinVisibility visibility) throws IOException {
        JsonObject body = new JsonObject();
        body.addProperty("variant", variant.name().toLowerCase(Locale.ROOT));
        body.addProperty("name", UUID.randomUUID().toString().substring(0, 6));
        body.addProperty("visibility", visibility.name().toLowerCase(Locale.ROOT));
        body.addProperty("url", toBase64EncodedImages(textureImage));
        if (cape != null) {
            body.addProperty("cape", cape.uuid.toString());
        }

        return fetch(HttpRequest.newBuilder()
                        .uri(URI.create(Ksin.INSTANCE.config.mineSkin.get().apiRoot.get() + "v2/generate"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                , jsonElement -> MineSkinResponse.GenerateResponse.parse(jsonElement.getAsJsonObject()));
    }

    public enum SkinVariant {
        CLASSIC,
        SLIM,
        UNKNOWN;
    }

    public enum SkinVisibility {
        PUBLIC,
        UNLISTED,
        PRIVATE;
    }

    public record Cape(
            UUID uuid,
            String alias,
            boolean supported
    ) {
    }


    public static sealed class MineSkinResponse {
        public final Map<String, String> errors;
        public final Map<String, String> warnings;
        public final Map<String, String> messages;

        protected MineSkinResponse(JsonObject object) {
            this.errors = object.has("errors") ? parseMessageMap(object.getAsJsonArray("errors")) : Collections.emptyMap();
            this.warnings = object.has("warnings") ? parseMessageMap(object.getAsJsonArray("warnings")) : Collections.emptyMap();
            this.messages = object.has("messages") ? parseMessageMap(object.getAsJsonArray("messages")) : Collections.emptyMap();
        }

        private Map<String, String> parseMessageMap(JsonArray jsonArray) {
            Map<String, String> map = new HashMap<>();
            for (JsonElement element : jsonArray) {
                JsonObject subObject = element.getAsJsonObject();
                map.put(subObject.getAsJsonPrimitive("code").getAsString(), subObject.getAsJsonPrimitive("message").getAsString());
            }
            return Collections.unmodifiableMap(map);
        }

        public static abstract sealed class CapesResponse extends MineSkinResponse {
            protected CapesResponse(JsonObject object) {
                super(object);
            }

            public static CapesResponse parse(JsonObject jsonObject) {
                if (jsonObject.getAsJsonPrimitive("success").getAsBoolean()) {
                    return new SuccessCapesResponse(jsonObject);
                } else {
                    return new FailureCapesResponse(jsonObject);
                }
            }

            public static final class SuccessCapesResponse extends CapesResponse {
                public final List<Cape> capes;

                SuccessCapesResponse(JsonObject object) {
                    super(object);
                    List<Cape> capes = new ArrayList<>();
                    for (JsonElement element : object.getAsJsonArray("capes")) {
                        JsonObject subJsonObject = element.getAsJsonObject();
                        capes.add(new Cape(
                                UUID.fromString(subJsonObject.getAsJsonPrimitive("uuid").getAsString()),
                                subJsonObject.getAsJsonPrimitive("alias").getAsString(),
                                Optional.ofNullable(subJsonObject.get("supported"))
                                        .filter(JsonElement::isJsonPrimitive)
                                        .map(JsonElement::getAsBoolean)
                                        .orElse(false)
                        ));
                    }
                    this.capes = Collections.unmodifiableList(capes);
                }
            }

            public static final class FailureCapesResponse extends CapesResponse {
                FailureCapesResponse(JsonObject object) {
                    super(object);
                }
            }
        }


        public static abstract sealed class GenerateResponse extends MineSkinResponse {
            protected GenerateResponse(JsonObject object) {
                super(object);
            }

            public static GenerateResponse parse(JsonObject jsonObject) {
                if (jsonObject.getAsJsonPrimitive("success").getAsBoolean()) {
                    return new SuccessGenerateResponse(jsonObject);
                } else {
                    return new FailureGenerateResponse(jsonObject);
                }
            }

            public static final class SuccessGenerateResponse extends GenerateResponse {
                public final String textureValue;
                public final String textureSignature;

                SuccessGenerateResponse(JsonObject object) {
                    super(object);
                    JsonObject dataObject = object.getAsJsonObject("skin")
                            .getAsJsonObject("texture")
                            .getAsJsonObject("data");

                    textureValue = dataObject.getAsJsonPrimitive("value").getAsString();
                    textureSignature = dataObject.getAsJsonPrimitive("signature").getAsString();
                }
            }

            public static final class FailureGenerateResponse extends GenerateResponse {
                FailureGenerateResponse(JsonObject object) {
                    super(object);
                }
            }
        }
    }
}
