package moe.caa.multilogin.ksin.logger;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@ApiStatus.Internal
public class KLogger {
    public final Logger handle;
    private boolean debugAsInfo = false;

    public KLogger(@NotNull Logger handle) {
        this.handle = handle;
    }

    public boolean isDebugAsInfo() {
        return debugAsInfo;
    }

    public void setDebugAsInfo(boolean flag) {
        this.debugAsInfo = flag;
    }

    public void debug(@NotNull String message, @NotNull Throwable throwable) {
        if (debugAsInfo) {
            info(message, throwable);
        } else {
            handle.debug(message, throwable);
        }
    }

    public void info(@NotNull String message, @NotNull Throwable throwable) {
        handle.info(message, throwable);
    }

    public void warn(@NotNull String message, @NotNull Throwable throwable) {
        handle.warn(message, throwable);
    }

    public void error(@NotNull String message, @NotNull Throwable throwable) {
        handle.error(message, throwable);
    }

    public void debug(@NotNull String message) {
        if (debugAsInfo) {
            info(message);
        } else {
            handle.debug(message);
        }
    }

    public void info(@NotNull String message) {
        handle.info(message);
    }

    public void warn(@NotNull String message) {
        handle.warn(message);
    }

    public void error(@NotNull String message) {
        handle.error(message);
    }
}
