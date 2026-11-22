package com.aliucord.aegires

import android.content.Context
import android.view.MenuItem
import com.aliucord.Utils
import com.aliucord.api.CommandsAPI
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.settings.WidgetSettings

@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class AegiresPlugin : Plugin() {
    companion object {
        const val KEY_AUTO_TOKEN = "auto_token_handoff"
        const val KEY_TOOLBAR_BTN = "show_toolbar_button"
    }

    init {
        settingsTab = SettingsTab(AegiresSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        patcher.after<WidgetSettings>("configureToolbar") {
            if (!settings.getBool(KEY_TOOLBAR_BTN, true)) return@after
            val activity = this.requireAppActivity()
            val toolbar = activity.u
            if (toolbar.menu.findItem(0xAE61) != null) return@after
            toolbar.menu.add(0, 0xAE61, 0, "Aegires")
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener {
                    launchAegires(activity)
                    true
                }
        }

        commands.registerCommand("aegires", "Open Aegires inside Aliucord") { ctx ->
            Utils.mainThread.post {
                launchAegires(ctx.currentChannel.let { Utils.appActivity })
            }
            CommandsAPI.CommandResult(null, null, false)
        }
    }

    private fun launchAegires(ctx: Context) {
        val autoToken = settings.getBool(KEY_AUTO_TOKEN, true)
        Utils.openPageWithProxy(ctx, AegiresFragment(autoToken))
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }
}
