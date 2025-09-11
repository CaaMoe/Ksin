package moe.caa.multilogin.ksin.internal.main;

import moe.caa.multilogin.ksin.internal.bootstrap.KsinBootstrap;
import moe.caa.multilogin.ksin.internal.logger.KLogger;
import org.jetbrains.annotations.NotNull;

public class Ksin {
    public static final @NotNull Ksin INSTANCE = new Ksin();
    public KLogger logger;
    public KsinBootstrap bootstrap;

    public void enable(@NotNull KsinBootstrap bootstrap) {
        this.bootstrap = bootstrap;
        this.logger = bootstrap.logger;
    }

    public void disable() {

    }
}
