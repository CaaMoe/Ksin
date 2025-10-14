package moe.caa.multilogin.ksin.internal.handler;

import moe.caa.multilogin.ksin.internal.main.Ksin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

import java.util.concurrent.ExecutionException;

public class MineSkinHttpHandlerTest {
    private static final String testTextureUrl = "https://namemc.com/texture/92c20ddbe38e7216.png";
    private static final MineSkinHttpHandler httpHandler = new MineSkinHttpHandler();

    @BeforeEach
    public void setUp() throws ConfigurateException {
        Ksin.INSTANCE.testInit();
        httpHandler.rebuildHttpClient();
    }

    @Test
    public void testGetTexture() {
        httpHandler.getTextureImage(testTextureUrl).thenAccept(image -> {
            Assertions.assertEquals(64, image.getWidth());
            Assertions.assertEquals(64, image.getHeight());
        }).join();
    }

    @Test
    public void testGetCapes() throws ExecutionException, InterruptedException {
        var response = httpHandler.getCapes().get();
        printInfo(response);
        if (response instanceof MineSkinHttpHandler.MineSkinResponse.CapesResponse.SuccessCapesResponse) {
            Assertions.assertNotEquals(0, ((MineSkinHttpHandler.MineSkinResponse.CapesResponse.SuccessCapesResponse) response).capes.size());
        }
    }

    @Test
    public void testGenerate() throws ExecutionException, InterruptedException {
        httpHandler.getTextureImage(testTextureUrl).thenApply(image -> {
            try {
                return httpHandler.generate(image, ((MineSkinHttpHandler.MineSkinResponse.CapesResponse.SuccessCapesResponse) httpHandler.getCapes().get()).capes.getFirst(), MineSkinHttpHandler.SkinVariant.SLIM, MineSkinHttpHandler.SkinVisibility.PUBLIC).thenAcceptAsync(response -> {
                    printInfo(response);
                }).join();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).get();
    }

    private void printInfo(MineSkinHttpHandler.MineSkinResponse response) {
        response.errors.forEach((code, message) -> {
            Ksin.INSTANCE.logger.error(code + " <-> " + message);
        });
        response.warnings.forEach((code, message) -> {
            Ksin.INSTANCE.logger.warn(code + " <-> " + message);
        });
        response.messages.forEach((code, message) -> {
            Ksin.INSTANCE.logger.info(code + " <-> " + message);
        });
    }

    @AfterEach
    public void tearDown() {
        Ksin.INSTANCE.logger = null;
    }
}
