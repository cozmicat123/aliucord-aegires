package com.aliucord.aegires

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.aliucord.Utils
import com.aliucord.fragments.SettingsPage
import com.aliucord.utils.DimenUtils

class AegiresSettings : SettingsPage() {
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
            text = "Milestone 1 PoC: launches full-screen proxy fragment + verifies shaded OkHttp 3.12.13 & RecyclerView."
            layoutParams = lp()
        }

        val launchBtn = Button(ctx).apply {
            text = "Launch Aegires (PoC)"
            layoutParams = lp()
            setOnClickListener {
                Utils.openPageWithProxy(ctx, AegiresPoCFragment())
            }
        }

        linearLayout.addView(info)
        linearLayout.addView(launchBtn)
    }
}
