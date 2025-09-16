package moe.caa.multilogin.ksin.internal.main;

import moe.caa.multilogin.ksin.internal.bootstrap.KsinBootstrap;
import moe.caa.multilogin.ksin.internal.logger.KLogger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Ksin {
    public static final @NotNull Ksin INSTANCE = new Ksin();
    public KLogger logger;
    public KsinBootstrap bootstrap;

    public void enable(@NotNull KsinBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.logger = bootstrap.logger;

        setupMetrics();
    }

    public void disable() {

    }


    private void setupMetrics() {
        try {
            Class<?> metricsFactoryClass = Class.forName("org.bstats.velocity.Metrics$Factory");

            Constructor<?> metricsFactoryConstructor = metricsFactoryClass.getConstructors()[0];
            Object metricsFactory = metricsFactoryConstructor.newInstance(bootstrap.server, bootstrap.logger.handle, bootstrap.dataDirectory);

            Method makeMethod = metricsFactoryClass.getMethod("make", Object.class, int.class);
            makeMethod.invoke(metricsFactory, bootstrap, 26924);
            logger.debug("Setup bStats metrics.");
        } catch (Throwable e) {
            logger.error("Failed to setup bStats metrics.", e);
        }
    }
}
