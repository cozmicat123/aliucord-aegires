package com.aliucord.aegires

import a.b.MainActivity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.discord.utilities.rest.RestAPI
import java.util.UUID

class AegiresFragment(
    private val autoTokenHandoff: Boolean = true,
) : Fragment() {
    private var aegiresHost: MainActivity? = null
    private var resultLauncher: ActivityResultLauncher<Intent>? = null
    private var pendingRequestCode: Int = -1
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

        if (act is ComponentActivity) {
            val key = "aegires_picker_${UUID.randomUUID()}"
            resultLauncher = act.activityResultRegistry.register(
                key,
                ActivityResultContracts.StartActivityForResult(),
            ) { result ->
                val code = pendingRequestCode
                pendingRequestCode = -1
                if (code >= 0) {
                    aegiresHost?.onActivityResult(code, result.resultCode, result.data)
                }
            }
        }

        val launchIntent = Intent()
        if (autoTokenHandoff) {
            val token = runCatching {
                RestAPI.AppHeadersProvider.INSTANCE.getAuthToken()
            }.getOrNull()
            if (!token.isNullOrBlank()) {
                launchIntent.putExtra("DISCORD_TOKEN", token)
            }
        }

        val host = MainActivity(act, this, launchIntent)
        host.setActivityResultLauncher { intent, requestCode ->
            val launcher = resultLauncher
            if (launcher != null) {
                pendingRequestCode = requestCode
                launcher.launch(intent)
            } else {
                @Suppress("DEPRECATION")
                startActivityForResult(intent, requestCode)
            }
        }
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
        requireActivity().onBackPressedDispatcher.addCallback(backCallback)
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
        backCallback.remove()
        resultLauncher?.unregister()
        resultLauncher = null
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
