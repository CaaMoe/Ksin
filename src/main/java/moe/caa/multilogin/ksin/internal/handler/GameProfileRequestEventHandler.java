package moe.caa.multilogin.ksin.internal.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.AwaitingEventExecutor;
import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.util.GameProfile;
import moe.caa.multilogin.ksin.internal.SkinVariant;
import moe.caa.multilogin.ksin.internal.configuration.MainConfig;
import moe.caa.multilogin.ksin.internal.main.Ksin;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.stream.Collectors;

public class GameProfileRequestEventHandler {
    private final String[] ALLOWED_DOMAINS = new String[]{".minecraft.net", ".mojang.com"};
    private final String[] BLOCKED_DOMAINS = new String[]{"bugs.mojang.com", "education.minecraft.net", "feedback.minecraft.net"};
    private PublicKey publicKey;

    public void init() {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(new byte[]{48, -126, 2, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 2, 15, 0, 48, -126, 2, 10, 2, -126, 2, 1, 0, -54, 80, 120, 7, -87, -71, -105, 62, -29, -62, -73, 5, -49, -95, 93, -3, -7, -33, 82, 23, 47, -105, 27, 19, 74, 126, 100, 32, -81, -10, 104, 97, -75, 11, 121, 12, -53, -123, 99, -121, 9, 113, 23, -96, 69, 81, 41, -33, -58, 19, 108, -64, -8, -31, -35, -104, -83, -112, 28, 68, 0, 86, 54, -26, -20, -28, 36, 84, 112, 31, 5, 64, -10, 103, 112, -115, -85, 33, 92, -126, -19, 71, -93, -86, 116, -75, -41, 21, 92, -108, 60, -123, 17, 44, -28, -106, -88, 99, 65, -75, -67, 60, 16, -34, 95, 50, 113, -68, -120, 82, -32, 16, -110, 1, 51, 27, 61, 6, -15, 119, -77, 127, -59, -122, -31, -14, 60, -88, -107, 77, -103, -105, 99, 69, -104, -31, -100, -104, -86, -6, 10, 71, -27, -29, 105, 52, 79, 62, -16, 108, 13, -82, 22, 35, 107, -75, -49, 21, -52, 120, 121, 104, 117, 61, -27, -100, 44, -43, -65, 35, -104, 4, -75, -53, -24, 34, 96, 7, 43, -114, -59, -57, 9, -14, 25, -41, 124, 95, 114, 99, -127, 116, 105, -30, -12, -13, 66, 115, -1, 127, -124, 4, -47, -68, 68, -18, 1, -54, 27, 65, 127, -125, 114, -16, -44, 58, -78, -43, -84, -115, -9, -108, 34, -15, -5, 109, 79, -8, -52, 38, 28, 96, -22, -72, 90, -78, 39, 92, 122, -110, -14, -86, -18, 14, 98, 37, -20, -2, 87, 92, 103, 26, 110, -20, -48, -78, -45, -33, -2, 29, -126, 68, 5, 41, 63, -13, -27, 29, 119, 12, -106, -9, -80, -114, 97, -108, -24, -57, -61, 43, -23, 98, 125, 39, 13, 99, 8, -49, -59, 26, 56, -100, -92, -58, 21, -105, -1, -47, -86, -121, 10, 31, -60, 2, -60, 50, 44, -86, -50, 33, -124, 68, -46, 80, -58, -35, -68, -63, -121, -54, 32, 32, -62, -64, -70, 11, 0, 124, 22, -6, 51, 121, -86, 10, -94, -102, 76, -64, 31, -3, 80, 35, -121, -111, 69, 38, 120, -46, -5, -74, -60, -7, -74, 68, 87, 74, -80, 107, 29, 66, 18, -125, -49, 36, -67, 95, 33, 8, -50, 75, -52, -29, -45, -50, -96, -114, -111, -111, 10, -44, -78, -9, -26, -45, 61, 55, -45, 71, -12, -119, -7, 85, 1, -28, 109, -120, 120, 34, 45, -112, -47, -23, 37, 125, -78, -126, -120, -59, 103, -39, -7, 22, 120, -5, 71, -42, 103, 56, -49, -117, -14, 124, -120, -116, -5, -73, 105, -29, -17, -75, -54, -41, -105, 34, -31, 31, -8, 111, 57, -14, 14, -116, -4, 78, -106, -91, -66, 59, -108, 48, 72, 83, -8, 56, 71, 50, 44, 5, 32, 13, -43, 32, -55, 109, -6, 103, -103, -127, -37, -50, 110, 41, 111, -30, -126, 28, -39, -80, -28, -34, -67, 85, 6, -39, 12, 3, 34, -45, -101, 33, 94, -94, -15, 16, -79, 21, -77, 118, -96, 49, -14, 61, -93, -71, -113, 91, 83, 104, 19, 114, 86, -33, 2, 3, 1, 0, 1});
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            publicKey = keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred during the generation of yggdrasil session public key.", e);
        }


        Ksin.INSTANCE.bootstrap.server.getEventManager().register(Ksin.INSTANCE.bootstrap, GameProfileRequestEvent.class,
                (AwaitingEventExecutor<GameProfileRequestEvent>) event -> EventTask.withContinuation(continuation -> {
                    Ksin.INSTANCE.asyncExecutor.execute(() -> {
                        try {
                            if (Ksin.INSTANCE.config.badSkinRepairerMethod.get() == MainConfig.BadSkinRepairerMethod.OFF)
                                return;

                            GameProfile originalProfile = event.getOriginalProfile();
                            GameProfile.Property originalTexturesProperty = originalProfile.getProperties().stream().filter(property -> property.getName().equals("textures")).findFirst().orElse(null);
                            if (originalTexturesProperty == null || originalTexturesProperty.getValue().isEmpty())
                                return;

                            JsonElement jsonElement = JsonParser.parseString(new String(Base64.getDecoder().decode(originalTexturesProperty.getValue()), StandardCharsets.UTF_8));
                            if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("textures")) {
                                JsonObject texturesJsonObject = jsonElement.getAsJsonObject().getAsJsonObject("textures");
                                if (texturesJsonObject.has("SKIN")) {
                                    JsonObject skinJsonObject = texturesJsonObject.getAsJsonObject("SKIN");
                                    if (skinJsonObject.has("url")) {
                                        String url = skinJsonObject.get("url").getAsString();
                                        SkinVariant variant = skinJsonObject.has("metadata")
                                                && skinJsonObject.getAsJsonObject("metadata").has("model")
                                                && skinJsonObject.getAsJsonObject("metadata").getAsJsonPrimitive("model")
                                                .getAsString().equals("slim") ? SkinVariant.SLIM : SkinVariant.CLASSIC;

                                        // 缓存修复
                                        GameProfile.Property repairResult = Ksin.INSTANCE.databaseHandler.queryRepairResult(url, "", variant);
                                        if (repairResult != null) {
                                            GameProfile gameProfile = event.getGameProfile();
                                            gameProfile.addProperty(repairResult);
                                            event.setGameProfile(gameProfile);
                                            Ksin.INSTANCE.logger.info("Applied repaired skin(cache) for player " + originalProfile.getName());
                                            return;
                                        }

                                        // 要不要释放事件
                                        if (Ksin.INSTANCE.config.badSkinRepairerMethod.get() == MainConfig.BadSkinRepairerMethod.ASYNC)
                                            continuation.resume();

                                        // 检查材质有没有坏掉
                                        if (isAllowedTextureDomain(url) && isSignatureValid(originalTexturesProperty))
                                            return;

                                        // 修!
                                        Ksin.INSTANCE.logger.info("Detected bad skin for url " + url + "( " + originalProfile.getName() + " ), repairing...");
                                        BufferedImage textureImage = Ksin.INSTANCE.mineSkinHttpHandler.getTextureImage(url).get();

                                        if (!isAllowedSizeTexture(textureImage)) {
                                            Ksin.INSTANCE.logger.warn("The skin of url " + url + "( " + originalProfile.getName() + " ) has an illegal size, cannot be repaired.");
                                            return;
                                        }
                                        MineSkinHttpHandler.MineSkinResponse.GenerateResponse generateResponse = Ksin.INSTANCE.mineSkinHttpHandler
                                                .generate(textureImage, null, variant, MineSkinHttpHandler.SkinVisibility.PUBLIC).get();

                                        switch (generateResponse) {
                                            case MineSkinHttpHandler.MineSkinResponse.GenerateResponse.FailureGenerateResponse failureGenerateResponse -> {
                                                Ksin.INSTANCE.logger.warn("Failed to repair skin for url " + url + "( " + originalProfile.getName() + " ), errors: " + failureGenerateResponse.errors.entrySet()
                                                        .stream().map(stringStringEntry -> stringStringEntry.getKey() + " (" + stringStringEntry.getValue() + ")").collect(Collectors.joining(", ")));
                                            }
                                            case MineSkinHttpHandler.MineSkinResponse.GenerateResponse.SuccessGenerateResponse successGenerateResponse -> {
                                                GameProfile.Property newTexturesProperty = new GameProfile.Property("textures", successGenerateResponse.textureValue, successGenerateResponse.textureSignature);
                                                GameProfile gameProfile = event.getGameProfile();
                                                gameProfile.addProperty(newTexturesProperty);
                                                event.setGameProfile(gameProfile);

                                                Ksin.INSTANCE.logger.info("Successfully repaired skin for url " + url + "( " + originalProfile.getName() + " )");
                                                Ksin.INSTANCE.logger.info("Applied repaired skin(" + Ksin.INSTANCE.config.badSkinRepairerMethod.get().name().toLowerCase() + ") for player " + originalProfile.getName());
                                                Ksin.INSTANCE.databaseHandler.saveRepairedSkin(url, "", variant, successGenerateResponse.textureValue, successGenerateResponse.textureSignature);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        } finally {
                            continuation.resume();
                        }
                    });
                })
        );
    }

    private boolean isAllowedSizeTexture(BufferedImage image) {
        if (image.getWidth() != 64) {
            return false;
        }
        if (!(image.getHeight() == 32 || image.getHeight() == 64)) {
            return false;
        }
        return true;
    }

    private boolean isSignatureValid(GameProfile.Property property) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
        Signature signature = Signature.getInstance("SHA1withRSA");
        signature.initVerify(publicKey);
        signature.update(property.getValue().getBytes());
        return signature.verify(Base64.getDecoder().decode(property.getSignature().getBytes()));
    }

    private boolean isAllowedTextureDomain(String url) {
        try {
            String domain = new URI(url).getHost();
            boolean allowed = false;
            for (String allowedDomain : ALLOWED_DOMAINS) {
                if (domain.endsWith(allowedDomain)) {
                    allowed = true;
                    break;
                }
            }
            if (!allowed) return false;
            for (String blockedDomain : BLOCKED_DOMAINS) {
                if (domain.endsWith(blockedDomain)) {
                    return false;
                }
            }
            return true;
        } catch (URISyntaxException ignored) {
            throw new IllegalArgumentException("Invalid URL '" + url + "'");
        }
    }
}