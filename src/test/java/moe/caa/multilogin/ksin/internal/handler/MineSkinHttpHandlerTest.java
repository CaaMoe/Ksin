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
        httpHandler.getTextureFromUrl(testTextureUrl).thenAcceptAsync(inputStream -> {
            try {
                BufferedImage image = ImageIO.read(inputStream);
                Assertions.assertEquals(64, image.getWidth());
                Assertions.assertEquals(64, image.getHeight());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).join();
    }

    @Test
    public void testGetSupportedCapes() throws ExecutionException, InterruptedException {
        var completableFuture = httpHandler.getSupportedCapes();
        for (MineSkinHttpHandler.SupportCape cape : completableFuture.get()) {
            Ksin.INSTANCE.logger.info("Supported cape: " + cape);
        }
        Assertions.assertNotEquals(0, completableFuture.get().size());
    }


    @AfterEach
    public void tearDown() {
        Ksin.INSTANCE.logger = null;
    }
}
