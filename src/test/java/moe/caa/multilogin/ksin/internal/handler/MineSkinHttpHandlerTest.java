package moe.caa.multilogin.ksin.internal.handler;

import moe.caa.multilogin.ksin.internal.main.Ksin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.spongepowered.configurate.ConfigurateException;

public class MineSkinHttpHandlerTest {
    private static final String testTextureUrl = "https://namemc.com/texture/92c20ddbe38e7216.png";
    private static final MineSkinHttpHandler httpHandler = new MineSkinHttpHandler();

    @BeforeEach
    public void setUp() throws ConfigurateException {
        Ksin.INSTANCE.testInit();
        httpHandler.rebuildHttpClient();
    }

    @Test
    public void test() {

    }

    @AfterEach
    public void tearDown() {
        Ksin.INSTANCE.logger = null;
    }
}
