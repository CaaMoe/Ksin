package moe.caa.multilogin.ksin.internal.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.NodePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Configuration {
    private final @NotNull List<@NotNull ParseableConfigurationValue<?>> configurationValues = new ArrayList<>();

    protected <E extends Enum<E>> @NotNull ConfigurationValue<E> enumConstant(@NotNull NodePath path, Class<E> enumClass) {
        return raw(path, node -> {
            String value = node.getString();
            if (value == null) {
                return null;
            }

            for (E constant : enumClass.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value '" + value + "' for enum " + enumClass.getName() + " at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")));

        });
    }

    protected <E extends Enum<E>> @NotNull ConfigurationValue<E> enumConstantOpt(@NotNull NodePath path, Class<E> enumClass, @NotNull E defaultValue) {
        return rawOpt(path, node -> {
            String value = node.getString();
            if (value == null) {
                return null;
            }

            for (E constant : enumClass.getEnumConstants()) {
                if (constant.name().equalsIgnoreCase(value)) {
                    return constant;
                }
            }
            throw new IllegalArgumentException("Invalid enum value '" + value + "' for enum " + enumClass.getName() + " at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")));

        }, defaultValue);
    }

    protected @NotNull ConfigurationValue<Boolean> bool(@NotNull NodePath path) {
        return raw(path, ConfigurationNode::getBoolean);
    }

    protected @NotNull ConfigurationValue<Boolean> boolOpt(@NotNull NodePath path, boolean defaultValue) {
        return rawOpt(path, ConfigurationNode::getBoolean, defaultValue);
    }

    protected @NotNull ConfigurationValue<Integer> integer(@NotNull NodePath path) {
        return raw(path, ConfigurationNode::getInt);
    }

    protected @NotNull ConfigurationValue<Integer> integerOpt(@NotNull NodePath path, int defaultValue) {
        return rawOpt(path, ConfigurationNode::getInt, defaultValue);
    }

    protected @NotNull ConfigurationValue<String> string(@NotNull NodePath path) {
        return raw(path, ConfigurationNode::getString);
    }

    protected @NotNull ConfigurationValue<String> stringOpt(@NotNull NodePath path, @NotNull String defaultValue) {
        return rawOpt(path, ConfigurationNode::getString, defaultValue);
    }

    protected <T extends Configuration> @NotNull ConfigurationValue<T> sub(@NotNull NodePath path, @NotNull T configurationInstance) {
        return new ConfigurationSubConfiguration<>(path, configurationInstance);
    }

    protected <T> @NotNull ConfigurationValue<T> raw(@NotNull NodePath path, @NotNull Function<@NotNull ConfigurationNode, @Nullable T> mapValue) {
        return new ConfigurationSpecifiedValue<>(path, mapValue, () -> {
            throw new IllegalArgumentException(
                    Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")) + " is a required value, but it is empty."
            );
        });
    }

    protected <T> @NotNull ConfigurationValue<T> rawOpt(@NotNull NodePath path, Function<@NotNull ConfigurationNode, @Nullable T> mapValue, Supplier<@NotNull T> defaultProvider) {
        return new ConfigurationSpecifiedValue<>(path, mapValue, defaultProvider);
    }

    protected <T> @NotNull ConfigurationValue<T> rawOpt(@NotNull NodePath path, Function<@NotNull ConfigurationNode, @Nullable T> mapValue, @NotNull T defaultValue) {
        return rawOpt(path, mapValue, (Supplier<T>) () -> defaultValue);
    }

    public void loadFrom(@NotNull ConfigurationNode node) {
        for (var configurationValue : configurationValues) {
            switch (configurationValue) {
                case ConfigurationSpecifiedValue<?> csv -> configurationValue.parse(node.node(csv.path));
                case ConfigurationSubConfiguration<?> csc -> configurationValue.parse(node.node(csc.path));
            }
        }
    }

    public sealed interface ConfigurationValue<T> {
        @NotNull T get();
    }

    private sealed interface ParseableConfigurationValue<T> extends ConfigurationValue<T> {
        void parse(@NotNull ConfigurationNode node);
    }

    private final class ConfigurationSubConfiguration<T extends Configuration> implements ParseableConfigurationValue<T> {
        private final @NotNull NodePath path;
        private final @NotNull T configurationInstance;

        public ConfigurationSubConfiguration(@NotNull NodePath path, @NotNull T configurationInstance) {
            this.path = path;
            this.configurationInstance = configurationInstance;

            configurationValues.add(this);
        }

        @Override
        public @NotNull T get() {
            return configurationInstance;
        }

        @Override
        public void parse(@NotNull ConfigurationNode node) {
            configurationInstance.loadFrom(node);
        }
    }

    private final class ConfigurationSpecifiedValue<T> implements ParseableConfigurationValue<T> {
        private final @NotNull NodePath path;
        private final @NotNull Function<@NotNull ConfigurationNode, @Nullable T> mapValue;
        private final @NotNull Supplier<@NotNull T> defaultProvider;
        private @Nullable T parsedValue;

        public ConfigurationSpecifiedValue(
                @NotNull NodePath path,
                @NotNull Function<@NotNull ConfigurationNode, @Nullable T> mapValue,
                @NotNull Supplier<@NotNull T> defaultProvider
        ) {
            this.path = path;
            this.mapValue = mapValue;
            this.defaultProvider = defaultProvider;

            configurationValues.add(this);
        }

        @Override
        public void parse(@NotNull ConfigurationNode node) {
            parsedValue = Objects.requireNonNullElseGet(mapValue.apply(node), defaultProvider);
        }

        @Override
        public @NotNull T get() {
            if (parsedValue == null) {
                throw new IllegalStateException("Configuration value at path " + Arrays.stream(path.array()).map(Object::toString).collect(Collectors.joining(".")) + " has not been parsed yet.");
            }
            return parsedValue;
        }
    }
}
