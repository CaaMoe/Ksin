package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfiguration = string(NodePath.path("database-configuration"));
    public final ConfigurationValue<BadSkinRepairerMethod> badSkinRepairerMethod = enumConstant(NodePath.path("bad-skin-repairer-method"), BadSkinRepairerMethod.class);
    public final ConfigurationValue<MineSkinConfig> mineSkin = sub(NodePath.path("mine-skin"), new MineSkinConfig());
    public final ConfigurationValue<ProxyConfig> proxy = sub(NodePath.path("proxy"), new ProxyConfig());
    public final ConfigurationValue<Integer> httpTimeout = integerOpt(NodePath.path("http-timeout"), 10000);

    public enum BadSkinRepairerMethod {
        LOGIN,
        ASYNC,
        OFF,
    }
}
