package moe.caa.multilogin.ksin.internal.handler;

import moe.caa.multilogin.ksin.internal.main.Ksin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

public class HttpHandlerTest {
    private static final String testTextureUrl = "https://namemc.com/texture/92c20ddbe38e7216.png";
    private static final HttpHandler httpHandler = new HttpHandler();

    @BeforeEach
    public void setUp() throws ConfigurateException {
        Ksin.INSTANCE.testInit();
        httpHandler.rebuildHttpClient();
    }

    @Test
    public void testGetTexture() {
        HttpRequest request = httpHandler.buildGetTextureRequest(testTextureUrl);
        httpHandler.sendAsyncRetry(request, HttpResponse.BodyHandlers.ofInputStream(), 3).thenAcceptAsync(inputStreamHttpResponse -> {
            try {
                BufferedImage image = ImageIO.read(inputStreamHttpResponse.body());
                Assertions.assertEquals(64, image.getWidth());
                Assertions.assertEquals(64, image.getHeight());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Test
    public void testGetSupportedCapes() throws ExecutionException, InterruptedException {
        Assertions.assertNotEquals(0, httpHandler.getSupportedCapes().get().size());
    }


    @AfterEach
    public void tearDown() {
        Ksin.INSTANCE.logger = null;
    }
}
