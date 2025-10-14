package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.spongepowered.configurate.NodePath;

public class MineSkinConfig extends Configuration {
    public final ConfigurationValue<String> apiRoot = raw(NodePath.path("api-root"), node -> {
        String string = node.getString("https://api.mineskin.org/");
        if (!string.endsWith("/")) {
            string = string + "/";
        }
        return string;
    });
    public final ConfigurationValue<String> apiKey = stringOpt(NodePath.path("api-key"), "");
}
