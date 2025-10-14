package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MineSkinConfig extends Configuration {
    public final ConfigurationValue<String> apiRoot = stringOpt(NodePath.path("api-root"), "https://api.mineskin.org");
    public final ConfigurationValue<String> apiKey = stringOpt(NodePath.path("api-key"), "");
}
