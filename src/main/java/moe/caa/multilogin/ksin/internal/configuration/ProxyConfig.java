package moe.caa.multilogin.ksin.internal.configuration;

import moe.caa.multilogin.ksin.internal.main.Ksin;
import moe.caa.multilogin.ksin.internal.util.Configuration;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.NodePath;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.util.Collections;
import java.util.List;

public class ProxyConfig extends Configuration {
    public final ConfigurationValue<Boolean> enabled = boolOpt(NodePath.path("enabled"), false);
    public final ConfigurationValue<Proxy.Type> type = enumConstantOpt(NodePath.path("type"), Proxy.Type.class, Proxy.Type.HTTP);
    public final ConfigurationValue<String> ip = stringOpt(NodePath.path("ip"), "localhost");
    public final ConfigurationValue<Integer> port = integerOpt(NodePath.path("port"), 7890);
    public final ConfigurationValue<String> username = stringOpt(NodePath.path("username"), "");
    public final ConfigurationValue<String> password = stringOpt(NodePath.path("password"), "");

    private Proxy generateProxy() {
        return new Proxy(type.get(), new java.net.InetSocketAddress(ip.get(), port.get()));
    }

    private Authenticator generateProxyAuthenticator() {
        return new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                if (getRequestorType() == RequestorType.PROXY) {
                    String host = getRequestingHost();
                    int port = getRequestingPort();
                    if (host.equalsIgnoreCase(ProxyConfig.this.ip.get()) && port == ProxyConfig.this.port.get()) {
                        String proxyUser = username.get();
                        String proxyPass = password.get();

                        if (proxyUser.isEmpty() || proxyPass.isEmpty()) {
                            return null;
                        }
                        return new PasswordAuthentication(proxyUser, proxyPass.toCharArray());
                    }
                }
                return super.getPasswordAuthentication();
            }
        };
    }

    public @NotNull HttpClient.Builder attachProxy(@NotNull HttpClient.Builder builder) {
        if (!enabled.get()) return builder;

        Proxy proxy = generateProxy();
        builder.proxy(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(proxy);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                Ksin.INSTANCE.logger.error("Failed to connect to proxy " + sa + " for URI " + uri, ioe);
            }
        });
        Authenticator authenticator = generateProxyAuthenticator();
        builder.authenticator(authenticator);
        return builder;
    }
}
