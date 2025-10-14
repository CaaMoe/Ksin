package moe.caa.multilogin.ksin.internal.bootstrap.dependency;

import org.jetbrains.annotations.NotNull;

public record Dependency(
        @NotNull String groupId,
        @NotNull String artifactId,
        @NotNull String version) {

    public static Dependency ofString(@NotNull String dependencyString) {
        String[] parts = dependencyString.split(":");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid dependency string format. Expected format: groupId:artifactId:version");
        }
        return new Dependency(parts[0], parts[1], parts[2]);
    }

    public @NotNull String getJarFilename() {
        return String.format("%s-%s.jar", artifactId, version);
    }

    public @NotNull String getJarPath() {
        return String.format("%s/%s/%s/%s", groupId.replace('.', '/'), artifactId, version, getJarFilename());
    }

    public @NotNull String getRelocatedJarPath() {
        return String.format("%s/%s/%s/relocated-%s", groupId.replace('.', '/'), artifactId, version, getJarFilename());
    }


    public @NotNull String generateJarDownloadURL(String repository) {
        return repository + "/" + getJarPath();
    }

    public @NotNull String generateJarDownloadSha1URL(String repository) {
        return repository + "/" + getJarPath() + ".sha1";
    }

    @Override
    public @NotNull String toString() {
        return groupId + ":" + artifactId + ":" + version;
    }
}
