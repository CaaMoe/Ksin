package moe.caa.multilogin.ksin.util.configuration

import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.NodePath
import kotlin.reflect.KProperty

internal open class Configuration {
    private val allConfigValues = mutableListOf<ConfigurationValue<*>>()

    protected fun <T : Any> raw(
        path: NodePath,
        mapValue: (ConfigurationNode) -> T?
    ): ConfigurationValue<T> = ConfigurationSpecifiedValueImpl(
        path = path,
        defaultProvider = {
            throw IllegalArgumentException("${path.array().joinToString(".")} is a required value, but it is empty.")
        },
        mapValue = mapValue
    )

    protected fun <T : Any> raw(
        path: NodePath,
        defaultProvider: () -> T,
        mapValue: (ConfigurationNode) -> T?
    ): ConfigurationValue<T> = ConfigurationSpecifiedValueImpl(
        path = path,
        defaultProvider = defaultProvider,
        mapValue = mapValue
    )

    protected fun <SUB : Configuration> sub(
        path: NodePath,
        configuration: SUB
    ): ConfigurationValue<SUB> = ConfigurationSectionValueImpl(
        path = path,
        configuration = configuration
    )

    protected fun string(path: NodePath) = raw(path) { it.string }
    protected fun string(path: NodePath, defaultValue: String) = raw(path, { defaultValue }) { it.string }

    fun loadFrom(node: ConfigurationNode) {
        allConfigValues.forEach { configValue ->
            when (configValue) {
                is ConfigurationSectionValueImpl -> configValue.tryParse(node.node(configValue.path))
                is ConfigurationSpecifiedValueImpl -> configValue.tryParse(node.node(configValue.path))
            }
        }
    }

    fun checkUnused(node: ConfigurationNode): Set<NodePath> {
        val usedPaths = collectUsedPaths()
        val existingPaths = collectExistingPaths(node)
        return existingPaths - usedPaths
    }

    private fun collectUsedPaths(): Set<NodePath> {
        return buildSet {
            allConfigValues.forEach { value ->
                when (value) {
                    is ConfigurationSectionValueImpl<*> -> {
                        val currentPath = value.path
                        val subUsedPaths = value.configuration.collectUsedPaths()
                        subUsedPaths.forEach { subPath ->
                            add(currentPath.plus(subPath))
                        }
                        add(currentPath)
                    }

                    is ConfigurationSpecifiedValueImpl<*> -> {
                        add(value.path)
                    }
                }
            }
        }
    }

    private fun collectExistingPaths(
        node: ConfigurationNode,
        relativePath: NodePath = NodePath.path()
    ): Set<NodePath> {
        return buildSet {
            if (!node.virtual()) {
                add(relativePath)
            }
            node.childrenMap().forEach { (key, child) ->
                val childRelativePath = relativePath.withAppendedChild(key)
                addAll(collectExistingPaths(child, childRelativePath))
            }
        }
    }

    sealed interface ConfigurationValue<T : Any> {
        fun tryParse(node: ConfigurationNode): T
        operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    }

    private inner class ConfigurationSectionValueImpl<SUB : Configuration>(
        val path: NodePath,
        val configuration: SUB
    ) : ConfigurationValue<SUB> {
        init {
            allConfigValues += this
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>) = configuration

        override fun tryParse(node: ConfigurationNode): SUB {
            configuration.loadFrom(node)
            return configuration
        }
    }

    private inner class ConfigurationSpecifiedValueImpl<T : Any>(
        val path: NodePath,
        private val defaultProvider: () -> T,
        private val mapValue: (ConfigurationNode) -> T?
    ) : ConfigurationValue<T> {
        init {
            allConfigValues += this
        }

        private var parsedValue: T? = null

        override fun tryParse(node: ConfigurationNode): T {
            val value = mapValue(node) ?: defaultProvider()
            parsedValue = value
            return value
        }

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return parsedValue ?: throw IllegalStateException(
                "${path.array().joinToString(".")} has not been parsed yet. Call tryParse() first."
            )
        }
    }
}