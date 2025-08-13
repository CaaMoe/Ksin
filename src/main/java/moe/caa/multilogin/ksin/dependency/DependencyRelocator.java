package moe.caa.multilogin.ksin.dependency;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
class DependencyRelocator {
    private final @NotNull DependencyHandler dependencyHandler;
    private final @NotNull Path relocatedDependenciesDirectory;
    @NotNull Map<@NotNull String, @NotNull String> relocations = new ConcurrentHashMap<>();
    private volatile @Nullable RelocateTool relocateTool;

    DependencyRelocator(@NotNull DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
        this.relocatedDependenciesDirectory = dependencyHandler.dependenciesDirectory.resolve("relocated");
    }

    @NotNull Path relocate(@NotNull Dependency dependency, @NotNull Path dependencyJarPath) throws Throwable {
        RelocateTool tool = relocateTool;
        if (tool == null) {
            synchronized (this) {
                if (relocateTool == null) {
                    relocateTool = new RelocateTool();
                }
            }
            return relocate(dependency, dependencyJarPath);
        }
        return tool.relocate(dependency, dependencyJarPath);
    }

    void close() throws IOException {
        RelocateTool tool = relocateTool;
        if (tool != null) {
            tool.close();
        }
    }

    private class RelocateTool {
        private final @NotNull URLClassLoader urlClassLoader;
        private final @NotNull MethodHandle jarRelocatorConstructor;
        private final @NotNull MethodHandle jarRelocatorRunMethod;

        private RelocateTool() throws IOException, NoSuchAlgorithmException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
            long startTimeMills = System.currentTimeMillis();
            dependencyHandler.logger.debug("Initializing dependency relocator tool...");
            List<Dependency> relocateToolDependencies = List.of(
                    new Dependency("org.ow2.asm", "asm", "9.2"),
                    new Dependency("org.ow2.asm", "asm-commons", "9.2"),
                    new Dependency("me.lucko", "jar-relocator", "1.7")
            );
            List<URL> dependencyURLs = new ArrayList<>();
            for (Dependency dependency : relocateToolDependencies) {
                dependencyURLs.add(dependencyHandler.dependencyDownloader.fetchDependency(dependency).toUri().toURL());
            }
            urlClassLoader = new URLClassLoader(dependencyURLs.toArray(new URL[0]), DependencyRelocator.class.getClassLoader());

            MethodHandles.Lookup lookup = MethodHandles.lookup();

            Class<?> jarRelocatorClass = urlClassLoader.loadClass("me.lucko.jarrelocator.JarRelocator");
            jarRelocatorConstructor = lookup.unreflectConstructor(jarRelocatorClass.getConstructor(File.class, File.class, Map.class));
            jarRelocatorRunMethod = lookup.unreflect(jarRelocatorClass.getMethod("run"));

            dependencyHandler.logger.debug("Initialized dependency relocator tool, took " +
                    (System.currentTimeMillis() - startTimeMills) + " ms");
        }

        @NotNull Path relocate(@NotNull Dependency dependency, @NotNull Path dependencyJarPath) throws Throwable {
            long startTimeMills = System.currentTimeMillis();
            Path relocatedDependencyJarPath = relocatedDependenciesDirectory.resolve(dependency.getJarPath());
            if (!Files.exists(relocatedDependencyJarPath.getParent())) {
                Files.createDirectories(relocatedDependencyJarPath.getParent());
            }
            Object jarRelocatorObject = jarRelocatorConstructor.invoke(
                    dependencyJarPath.toFile(),
                    relocatedDependencyJarPath.toFile(),
                    relocations
            );

            jarRelocatorRunMethod.invoke(jarRelocatorObject);

            dependencyHandler.logger.debug(
                    "Relocated dependency " + dependencyJarPath.toAbsolutePath() +
                            " to " + relocatedDependencyJarPath.toAbsolutePath() +
                            ", took " + (System.currentTimeMillis() - startTimeMills) + " ms");
            return relocatedDependencyJarPath;
        }

        private void close() throws IOException {
            urlClassLoader.close();
        }
    }
}
