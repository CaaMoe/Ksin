package moe.caa.multilogin.ksin.internal.main;

import moe.caa.multilogin.ksin.internal.bootstrap.KsinBootstrap;
import moe.caa.multilogin.ksin.internal.configuration.MainConfig;
import moe.caa.multilogin.ksin.internal.logger.KLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;
import org.slf4j.LoggerFactory;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Ksin {
    public static final @NotNull Ksin INSTANCE = new Ksin();
    public final MainConfig config = new MainConfig();
    public final Executor asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    public KLogger logger;
    public KsinBootstrap bootstrap;

    @TestOnly
    public void testInit() throws ConfigurateException {
        logger = new KLogger(LoggerFactory.getLogger(Ksin.class));
        logger.setDebugAsInfo(true);

        config.loadFrom(GsonConfigurationLoader.builder().buildAndLoadString("{}"));
    }

    public void enable(@NotNull KsinBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.logger = bootstrap.logger;

        setupMetrics();
    }

    public void disable() {

    }


    void setupMetrics() {
        try {
            Class<?> metricsFactoryClass = Class.forName("org.bstats.velocity.Metrics$Factory");

            Constructor<?> metricsFactoryConstructor = metricsFactoryClass.getDeclaredConstructors()[0];
            metricsFactoryConstructor.setAccessible(true);
            Object metricsFactory = metricsFactoryConstructor.newInstance(bootstrap.server, bootstrap.logger.handle, bootstrap.dataDirectory);

            Method makeMethod = metricsFactoryClass.getMethod("make", Object.class, int.class);
            makeMethod.invoke(metricsFactory, bootstrap, 26924);
            logger.debug("Setup bStats metrics.");
        } catch (Throwable e) {
            logger.error("Failed to setup bStats metrics.", e);
        }
    }
}
