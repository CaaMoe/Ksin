package moe.caa.multilogin.ksin.internal.bootstrap.dependency;

import moe.caa.multilogin.ksin.internal.logger.KLogger;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class DependencyHandler {
    final @NotNull Path dependenciesDirectory;
    final @NotNull KLogger logger;

    final @NotNull DependencyDownloader dependencyDownloader;
    final @NotNull DependencyRelocator dependencyRelocator;

    public DependencyHandler(@NotNull Path dependenciesDirectory, @NotNull KLogger logger) {
        this.dependenciesDirectory = dependenciesDirectory;
        this.logger = logger;

        this.dependencyDownloader = new DependencyDownloader(this);
        this.dependencyRelocator = new DependencyRelocator(this);
    }

    public void addDependencyRepository(@NotNull String repositoryUrl) {
        if (repositoryUrl.endsWith("/")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 1);
        }
        dependencyDownloader.repositories.add(repositoryUrl);
    }

    public void addRelocation(@NotNull String original, @NotNull String relocated) {
        dependencyRelocator.relocations.put(original, relocated);
    }

    public void addExclude(@NotNull String pattern) {
        dependencyRelocator.excludes.add(pattern);
    }

    public @NotNull Path handleDependency(@NotNull Dependency dependency) throws Throwable {
        Path dependencyPath = dependencyDownloader.fetchDependency(dependency);
        return dependencyRelocator.relocate(dependency, dependencyPath);
    }

    public void close() throws Throwable {
        dependencyRelocator.close();
    }
}
