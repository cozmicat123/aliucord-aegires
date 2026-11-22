package com.aliucord.aegires

import android.content.Context
import android.view.MenuItem
import com.aliucord.Utils
import com.aliucord.annotations.AliucordPlugin
import com.aliucord.api.CommandsAPI
import com.aliucord.entities.Plugin
import com.aliucord.patcher.after
import com.discord.widgets.settings.WidgetSettings

@AliucordPlugin(requiresRestart = false)
@Suppress("unused")
class AegiresPlugin : Plugin() {
    companion object {
        const val KEY_AUTO_TOKEN = "auto_token_handoff"
        const val KEY_TOOLBAR_BTN = "show_toolbar_button"
        private const val MENU_ID_AEGIRES = 0xAE61
    }

    init {
        settingsTab = SettingsTab(AegiresSettings::class.java).withArgs(settings)
    }

    override fun start(context: Context) {
        patcher.after<WidgetSettings>("configureToolbar") {
            val activity = this.requireAppActivity()
            val toolbar = activity.u ?: return@after
            if (!settings.getBool(KEY_TOOLBAR_BTN, true)) {
                toolbar.menu.removeItem(MENU_ID_AEGIRES)
                return@after
            }
            if (toolbar.menu.findItem(MENU_ID_AEGIRES) != null) return@after
            toolbar.menu.add(0, MENU_ID_AEGIRES, 0, "Aegires")
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setOnMenuItemClickListener {
                    launchAegires(activity)
                    true
                }
        }

        commands.registerCommand("aegires", "Open Aegires inside Aliucord") {
            Utils.mainThread.post {
                Utils.appActivity?.let { launchAegires(it) }
            }
            CommandsAPI.CommandResult(null, null, false)
        }
    }

    private fun launchAegires(ctx: Context) {
        val autoToken = settings.getBool(KEY_AUTO_TOKEN, true)
        Utils.openPageWithProxy(ctx, AegiresFragment.newInstance(autoToken))
    }

    override fun stop(context: Context) {
        patcher.unpatchAll()
        commands.unregisterAll()
    }
}
