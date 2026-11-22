package com.aliucord.aegires

import android.content.Context
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin

@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class AegiresPlugin : Plugin() {
    init {
        settingsTab = SettingsTab(AegiresSettings::class.java)
    }

    override fun start(context: Context) {
        logger.info("Aegires PoC loaded")
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
    }
}
