package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MainConfig extends Configuration {
    public final ConfigurationValue<String> databaseConfiguration = string(NodePath.path("database-configuration"));
    public final ConfigurationValue<BadSkinRestorerMethod> badSkinRestorerMethod = enumConstant(NodePath.path("bad-skin-restorer-method"), BadSkinRestorerMethod.class);
    public final ConfigurationValue<MineSkinConfig> mineSkin = sub(NodePath.path("mine-skin"), new MineSkinConfig());


    public enum BadSkinRestorerMethod {
        LOGIN,
        ASYNC,
        OFF,
    }
}
