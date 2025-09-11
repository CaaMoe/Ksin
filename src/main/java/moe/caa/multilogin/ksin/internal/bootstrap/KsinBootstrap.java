package moe.caa.multilogin.ksin.internal.bootstrap;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import moe.caa.multilogin.ksin.internal.bootstrap.dependency.Dependency;
import moe.caa.multilogin.ksin.internal.bootstrap.dependency.DependencyHandler;
import moe.caa.multilogin.ksin.internal.logger.KLogger;
import moe.caa.multilogin.ksin.internal.main.Ksin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Plugin(id = KsinBootstrap.PLUGIN_ID)
public class KsinBootstrap {
    public static final String PLUGIN_ID = "ksin";

    public final @NotNull ProxyServer server;
    public final @NotNull KLogger logger;
    public final @NotNull Path dataDirectory;
    public final @NotNull DependencyHandler dependencyHandler;


    @Inject
    public KsinBootstrap(
            @NotNull ProxyServer server,
            @NotNull Logger logger,
            @NotNull @DataDirectory Path dataDirectory
    ) {
        this.server = server;
        this.logger = new KLogger(logger);
        this.dataDirectory = dataDirectory;
        this.dependencyHandler = new DependencyHandler(dataDirectory.resolve("libraries"), this.logger);
    }

    @Subscribe
    public void onInitialize(@NotNull ProxyInitializeEvent event) {
        try {
            initialize();
        } catch (Throwable e) {
            logger.error("Failed to initialize Ksin", e);
        }
    }

    @Subscribe
    public void onDisable(@NotNull ProxyShutdownEvent event) {
        try {
            Ksin.INSTANCE.disable();
            dependencyHandler.close();
            logger.info("Disabled ksin.");
        } catch (Throwable e) {
            logger.error("Failed to disable Ksin", e);
        }

    }

    private void initialize() throws Throwable {
        logger.info("Enabling Ksin...");
        checkLoggerDebugAsInfoFlag();
        initDependency();

        Ksin.INSTANCE.enable(this);
        logger.info("Enabled, using Ksin v" + server.getPluginManager().ensurePluginContainer(this).getDescription().getVersion().orElse("unknown") + ".");
    }


    private void initDependency() throws Throwable {
        // repository
        List<String> repositories = Arrays.stream(new String(
                        Objects.requireNonNull(readNestResource("repositories.txt"), "Failed to read repositories.txt(nest), it may not exist."),
                        StandardCharsets.UTF_8).split("\\r?\\n"))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .filter(e -> !e.startsWith("#"))
                .toList();
        for (String repository : repositories) {
            dependencyHandler.addDependencyRepository(repository);
            logger.debug("Added dependency repository: " + repository);
        }
        // outside repository
        Path outsideRepositories = dataDirectory.resolve("repositories.txt");
        if (Files.exists(outsideRepositories)) {
            List<String> outsideRepositoriesList = Files.readAllLines(outsideRepositories).stream()
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .filter(e -> !e.startsWith("#"))
                    .toList();
            for (String outsideRepository : outsideRepositoriesList) {
                dependencyHandler.addDependencyRepository(outsideRepository);
                logger.info("Added outside dependency repository: " + outsideRepository);
            }
        }

        // relocation
        List<String[]> relocations = Arrays.stream(new String(
                        Objects.requireNonNull(readNestResource("relocations.txt"), "Failed to read relocations.txt(nest), it may not exist."),
                        StandardCharsets.UTF_8).split("\\r?\\n"))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .filter(e -> !e.startsWith("#"))
                .map(s -> s.split("\\s+"))
                .toList();
        if (relocations.isEmpty()) {
            throw new IOException("No relocations found in the relocations.txt(nest).");
        }
        for (String[] relocation : relocations) {
            String original = relocation[0];
            String relocated = relocation[1];
            dependencyHandler.addRelocation(original, relocated);
            logger.debug("Added dependency relocation: " + original + " -> " + relocated);
        }
        // outside repository
        Path outsideRelocations = dataDirectory.resolve("relocations.txt");
        if (Files.exists(outsideRelocations)) {
            List<String[]> outsideRelocationsList = Files.readAllLines(outsideRelocations).stream()
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .filter(e -> !e.startsWith("#"))
                    .map(s -> s.split("\\s+"))
                    .toList();
            for (String[] outsideRelocation : outsideRelocationsList) {
                String original = outsideRelocation[0];
                String relocated = outsideRelocation[1];
                dependencyHandler.addRelocation(original, relocated);
                logger.info("Added outside dependency relocation: " + original + " -> " + relocated);
            }
        }

        // dependency
        List<String> dependencies = Arrays.stream(new String(
                        Objects.requireNonNull(readNestResource("dependencies.txt"), "Failed to read dependencies.txt(nest), it may not exist."),
                        StandardCharsets.UTF_8).split("\\r?\\n"))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .filter(e -> !e.startsWith("#"))
                .toList();
        for (String dependencyStr : dependencies) {
            Dependency dependency = Dependency.ofString(dependencyStr);
            server.getPluginManager().addToClasspath(KsinBootstrap.this, dependencyHandler.handleDependency(dependency));
            logger.debug("Loaded dependency: " + dependency);
        }
        // outside dependency
        Path outsideDependencies = dataDirectory.resolve("dependencies.txt");
        if (Files.exists(outsideDependencies)) {
            List<String> outsideDependenciesList = Files.readAllLines(outsideDependencies).stream()
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .filter(e -> !e.startsWith("#"))
                    .toList();
            for (String outsideDependencyStr : outsideDependenciesList) {
                Dependency dependency = Dependency.ofString(outsideDependencyStr);
                server.getPluginManager().addToClasspath(KsinBootstrap.this, dependencyHandler.handleDependency(dependency));
                logger.info("Loaded outside dependency: " + dependency);
            }
        }
    }

    public void checkLoggerDebugAsInfoFlag() {
        if (Files.exists(dataDirectory.resolve("debug"))) {
            if (!logger.isDebugAsInfo()) {
                logger.setDebugAsInfo(true);
                logger.info("Debug mode is enabled. All debug logs will be printed as info logs.");
            }
        } else {
            if (logger.isDebugAsInfo()) {
                logger.setDebugAsInfo(false);
                logger.info("Debug mode is disabled. Debug logs will not be printed as info logs.");
            }
        }
    }

    public byte @Nullable [] readNestResource(@NotNull String filename) throws IOException {
        URL url = getClass().getClassLoader().getResource(filename);
        if (url == null) {
            return null;
        }

        URLConnection connection = url.openConnection();
        try (InputStream inputStream = connection.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }
}
