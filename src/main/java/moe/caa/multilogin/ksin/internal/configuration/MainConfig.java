package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfiguration = stringOpt(NodePath.path("database-configuration"), "hikari.properties");
    public final ConfigurationValue<BadSkinRepairerMethod> badSkinRepairerMethod = enumConstantOpt(NodePath.path("bad-skin-repairer-method"), BadSkinRepairerMethod.class, BadSkinRepairerMethod.LOGIN);
    public final ConfigurationValue<MineSkinConfig> mineSkin = sub(NodePath.path("mine-skin"), new MineSkinConfig());
    public final ConfigurationValue<ProxyConfig> proxy = sub(NodePath.path("proxy"), new ProxyConfig());
    public final ConfigurationValue<Integer> httpTimeout = integerOpt(NodePath.path("http-timeout"), 10000);
    public final ConfigurationValue<Integer> httpRetries = integerOpt(NodePath.path("http-retries"), 3);

    public enum BadSkinRepairerMethod {
        LOGIN,
        ASYNC,
        OFF,
    }
}
