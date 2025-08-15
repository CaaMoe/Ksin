package moe.caa.multilogin.ksin.main

import moe.caa.multilogin.ksin.bootstrap.KsinBootstrap
import moe.caa.multilogin.ksin.logger.KLogger
import org.bstats.velocity.Metrics
import kotlin.reflect.jvm.isAccessible


internal object Ksin : KsinBootstrap.IKsin {
    private lateinit var bootstrap: KsinBootstrap
    private lateinit var logger: KLogger

    override fun enable(bootstrap: KsinBootstrap) {
        this.bootstrap = bootstrap
        this.logger = bootstrap.logger

        setupBStats()
    }

    override fun disable() {
    }

    private fun setupBStats() {
        runCatching {
            val metricsFactoryConstructorFunction = Metrics.Factory::class.constructors.first()
            metricsFactoryConstructorFunction.isAccessible = true
            val metricsFactory = metricsFactoryConstructorFunction.call(
                bootstrap.server,
                bootstrap.logger.handle,
                bootstrap.dataDirectory
            )
            metricsFactory.make(
                bootstrap,
                26924
            )
        }.onFailure {
            logger.error("Failed to setup bStats.", it)
        }
    }
}