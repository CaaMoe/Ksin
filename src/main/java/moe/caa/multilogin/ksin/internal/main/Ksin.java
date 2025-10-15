package moe.caa.multilogin.ksin.internal.main;

import moe.caa.multilogin.ksin.internal.bootstrap.KsinBootstrap;
import moe.caa.multilogin.ksin.internal.configuration.MainConfig;
import moe.caa.multilogin.ksin.internal.database.DatabaseHandler;
import moe.caa.multilogin.ksin.internal.handler.GameProfileRequestEventHandler;
import moe.caa.multilogin.ksin.internal.handler.MineSkinHttpHandler;
import moe.caa.multilogin.ksin.internal.logger.KLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Ksin {
    public static final @NotNull Ksin INSTANCE = new Ksin();
    public final MainConfig config = new MainConfig();
    public final Executor asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    public KLogger logger;
    public KsinBootstrap bootstrap;
    public DatabaseHandler databaseHandler;
    public MineSkinHttpHandler mineSkinHttpHandler;

    public void enable(@NotNull KsinBootstrap bootstrap) throws IOException {
        this.bootstrap = bootstrap;
        this.logger = bootstrap.logger;
        this.databaseHandler = DatabaseHandler.INSTANCE;
        this.mineSkinHttpHandler = new MineSkinHttpHandler();

        reload();
        setupMetrics();

        databaseHandler.initDatabase();
        new GameProfileRequestEventHandler().init();
    }

    public void reload() throws IOException {
        config.loadFrom(HoconConfigurationLoader.builder().path(saveResource("config.conf", false)).build().load());
        mineSkinHttpHandler.rebuildHttpClient();
    }

    public void disable() {
        databaseHandler.close();
    }

    public @Nullable InputStream getResourceAsStream(String resourcePath) {
        return getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    public Path saveResource(String resourcePath, boolean replace) throws IOException {
        Path outputPath = bootstrap.dataDirectory.resolve(resourcePath);
        if (replace || !Files.exists(outputPath)) {
            try (InputStream stream = getResourceAsStream(resourcePath)) {

                Path parentPath = outputPath.getParent();
                if (parentPath != null && !Files.exists(parentPath)) {
                    Files.createDirectories(parentPath);
                }
                Files.copy(stream, outputPath);
            }
        }
        return outputPath;
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
