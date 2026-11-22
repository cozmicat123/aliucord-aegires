package com.aliucord.aegires

import a.b.MainActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

class AegiresFragment : Fragment() {
    private var aegiresHost: MainActivity? = null
    private var prevSoftInputMode: Int = 0
    private var prevStatusBarColor: Int = 0
    private var prevNavBarColor: Int = 0
    private var hasSavedWindowState: Boolean = false

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val host = aegiresHost
            if (host != null && host.handleBackPressed()) {
                return
            }
            isEnabled = false
            activity?.onBackPressedDispatcher?.onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val act = requireActivity()
        act.window?.let { w ->
            prevSoftInputMode = w.attributes.softInputMode
            prevStatusBarColor = w.statusBarColor
            prevNavBarColor = w.navigationBarColor
            hasSavedWindowState = true
        }

        val host = MainActivity(act, this, Intent())
        aegiresHost = host
        host.onCreate(savedInstanceState)

        val root = host.rootFrame
        root.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    override fun onResume() {
        super.onResume()
        aegiresHost?.onResume()
    }

    override fun onPause() {
        aegiresHost?.onPause()
        super.onPause()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        aegiresHost?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        aegiresHost?.onDestroy()
        aegiresHost = null
        if (hasSavedWindowState) {
            activity?.window?.let { w ->
                w.setSoftInputMode(prevSoftInputMode)
                w.statusBarColor = prevStatusBarColor
                w.navigationBarColor = prevNavBarColor
            }
            hasSavedWindowState = false
        }
        super.onDestroyView()
    }
}
