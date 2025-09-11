package moe.caa.multilogin.ksin.internal.bootstrap.dependency;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

class DependencyDownloader {
    final @NotNull DependencyHandler dependencyHandler;
    final @NotNull Set<String> repositories = new LinkedHashSet<>();

    protected DependencyDownloader(@NotNull DependencyHandler dependencyHandler) {
        this.dependencyHandler = dependencyHandler;
    }

    protected @NotNull Path fetchDependency(@NotNull Dependency dependency) throws IOException, NoSuchAlgorithmException {
        String expectSha1Hex = new String(downloadFromAnyURL(dependency + "'s sha1", repositories.stream().map(dependency::generateJarDownloadSha1URL).toList()), StandardCharsets.UTF_8);
        Path savePath = dependencyHandler.dependenciesDirectory.resolve(dependency.getJarPath());
        if (Files.exists(savePath)) {
            String activeSha1Hex = bytesToHex(MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(savePath)));
            if (activeSha1Hex.equals(expectSha1Hex)) {
                return savePath;
            }
            dependencyHandler.logger.warn("Dependency " + dependency + " already exists in " + savePath.toAbsolutePath() +
                    " but SHA1 mismatch: expected " + expectSha1Hex + ", got " + activeSha1Hex + ". Redownloading.");
        }
        byte[] jarBytes = downloadFromAnyURL(dependency + "'s jar", repositories.stream().map(dependency::generateJarDownloadURL).toList());
        if (!Files.exists(savePath.getParent())) {
            Files.createDirectories(savePath.getParent());
        }
        Files.write(savePath, jarBytes);

        String activeSha1Hex = bytesToHex(MessageDigest.getInstance("SHA-1").digest(Files.readAllBytes(savePath)));
        if (!activeSha1Hex.equals(expectSha1Hex)) {
            throw new IOException("Downloaded dependency " + dependency + " in " + savePath.toAbsolutePath() + " but SHA1 mismatch: expected " + expectSha1Hex + ", got " + activeSha1Hex);
        }
        return savePath;
    }


    private @NotNull String bytesToHex(byte @NotNull [] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte @NotNull [] downloadFromAnyURL(@NotNull String name, @NotNull List<String> urls) throws IOException {
        IOException exception = null;
        for (String url : urls) {
            try {
                return download(url);
            } catch (Exception e) {
                if (exception == null) {
                    exception = new IOException("Failed to download " + name + " from any URLs: " + urls);
                }
                exception.addSuppressed(e);
            }
        }
        if (exception == null) {
            exception = new IOException("Failed to download " + name + " from any URLs: " + urls);
        }
        throw exception;
    }

    private byte @NotNull [] download(@NotNull String url) throws IOException {
        dependencyHandler.logger.debug("Downloading from " + url);
        long currentTimeMills = System.currentTimeMillis();
        try (InputStream inputStream = URI.create(url).toURL().openStream()) {
            byte[] bytes = inputStream.readAllBytes();
            dependencyHandler.logger.debug("Downloaded " + bytes.length + " bytes from " + url + ", took " + (System.currentTimeMillis() - currentTimeMills) + " ms.");
            return bytes;
        }
    }
}
