package moe.caa.multilogin.ksin.internal.configuration;

import com.zaxxer.hikari.HikariConfig;
import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

public class DatabaseConfig extends Configuration {
    private final ConfigurationValue<String> catalog = stringOpt(NodePath.path("catalog"), "");
    private final ConfigurationValue<Boolean> autoCommit = boolOpt(NodePath.path("autoCommit"), false);


    public @NotNull HikariConfig buildHikariConfig() {
        HikariConfig config = new HikariConfig();
        config.setCatalog(catalog.get());
        config.setAutoCommit(autoCommit.get());

        return config;
    }
}
