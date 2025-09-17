package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MineSkinConfig extends Configuration {
    public final ConfigurationValue<String> apiRoot = string(NodePath.path("api-root"));
    public final ConfigurationValue<String> apiKey = string(NodePath.path("api-key"));
    public final ConfigurationValue<ProxyConfig> proxy = sub(NodePath.path("proxy"), new ProxyConfig());
}
