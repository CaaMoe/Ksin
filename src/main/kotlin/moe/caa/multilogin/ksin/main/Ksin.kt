package moe.caa.multilogin.ksin.main

import moe.caa.multilogin.ksin.bootstrap.KsinBootstrap

internal object Ksin : KsinBootstrap.IKsin {
    private lateinit var bootstrap: KsinBootstrap

    override fun enable(bootstrap: KsinBootstrap) {
        this.bootstrap = bootstrap
    }

    override fun disable() {

    }
}