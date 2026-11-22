package com.aliucord.aegires

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.api.SettingsAPI
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils
import com.discord.views.CheckedSetting

class AegiresSettings(private val prefs: SettingsAPI) : SettingsPage() {
    override fun onViewBound(view: View) {
        super.onViewBound(view)
        setActionBarTitle("Aegires")
        val ctx = view.context
        val pad = DimenUtils.defaultPadding

        fun lp() = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { setMargins(pad, pad / 2, pad, pad / 2) }

        val info = TextView(ctx).apply {
            text = "Run Aegires directly inside Aliucord (unrooted, isolated storage)."
            layoutParams = lp()
        }

        val launchBtn = Button(ctx).apply {
            text = "Launch Aegires"
            layoutParams = lp()
            setOnClickListener {
                val autoToken = prefs.getBool(AegiresPlugin.KEY_AUTO_TOKEN, true)
                Utils.openPageWithProxy(ctx, AegiresFragment.newInstance(autoToken))
            }
        }

        val autoTokenToggle = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Auto-login with Aliucord account",
            "Pass your active Discord session token directly into Aegires on launch.",
        ).apply {
            isChecked = prefs.getBool(AegiresPlugin.KEY_AUTO_TOKEN, true)
            setOnCheckedListener { checked ->
                prefs.setBool(AegiresPlugin.KEY_AUTO_TOKEN, checked)
            }
        }

        val toolbarToggle = Utils.createCheckedSetting(
            ctx,
            CheckedSetting.ViewType.SWITCH,
            "Show Settings toolbar button",
            "Adds an \"Aegires\" launch button to the Discord User Settings top bar.",
        ).apply {
            isChecked = prefs.getBool(AegiresPlugin.KEY_TOOLBAR_BTN, true)
            setOnCheckedListener { checked ->
                prefs.setBool(AegiresPlugin.KEY_TOOLBAR_BTN, checked)
            }
        }

        val clearTokenBtn = Button(ctx).apply {
            text = "Clear Aegires saved token"
            layoutParams = lp()
            setOnClickListener {
                ctx.applicationContext
                    .getSharedPreferences("aegires_auth", Context.MODE_PRIVATE)
                    .edit()
                    .remove("token")
                    .apply()
                Utils.showToast("Aegires saved token cleared")
            }
        }

        linearLayout.addView(info)
        linearLayout.addView(launchBtn)
        linearLayout.addView(autoTokenToggle)
        linearLayout.addView(toolbarToggle)
        linearLayout.addView(clearTokenBtn)
    }
}
