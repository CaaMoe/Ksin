package moe.caa.multilogin.ksin.internal.handler;

import moe.caa.multilogin.ksin.internal.main.Ksin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

public class HttpHandlerTest {
    private static final HttpHandler httpHandler = new HttpHandler();

    @BeforeEach
    public void setUp() {
        Ksin.INSTANCE.testInit();
        httpHandler.testInit();
    }

    @Test
    public void test() throws ExecutionException, InterruptedException {

//        HttpRequest request = httpHandler.buildGetTextureRequest("https://namemc.com/texture/92c20ddbe38e7216.png");
//        System.out.println("result: " + httpHandler.sendAsyncRetry(request, HttpResponse.BodyHandlers.ofString(), 3).get());
    }

    @AfterEach
    public void tearDown() {
        Ksin.INSTANCE.logger = null;
    }
}
