package com.aliucord.aegires

import aegires.okhttp3.OkHttpClient
import aegires.okhttp3.Request
import aegires.okio.ByteString
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.concurrent.TimeUnit

class AegiresPoCFragment : Fragment() {
    private lateinit var statusView: TextView
    private val rows = mutableListOf<String>()
    private lateinit var adapter: SimpleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val ctx = requireContext()
        val dp = ctx.resources.displayMetrics.density
        val pad = (16 * dp).toInt()

        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0E1014"))
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            setPadding(pad, pad, pad, pad)
        }

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(pad + bars.left, pad + bars.top, pad + bars.right, pad + bars.bottom)
            insets
        }

        val title = TextView(ctx).apply {
            text = "Aegires Unrooted Host — Milestone 1 PoC"
            textSize = 18f
            setTextColor(Color.WHITE)
        }

        statusView = TextView(ctx).apply {
            text = "Running shaded OkHttp TLS 1.3 probe..."
            textSize = 13f
            setTextColor(Color.parseColor("#B5BAC1"))
            setPadding(0, (8 * dp).toInt(), 0, (12 * dp).toInt())
        }

        adapter = SimpleAdapter(rows)
        val recycler = RecyclerView(ctx).apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = this@AegiresPoCFragment.adapter
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f,
            )
        }

        val buttons = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, (12 * dp).toInt(), 0, 0)
        }

        val retestBtn = Button(ctx).apply {
            text = "Re-test OkHttp"
            setOnClickListener { runProbe(root) }
        }

        val closeBtn = Button(ctx).apply {
            text = "Close"
            setOnClickListener { activity?.finish() }
        }

        buttons.addView(retestBtn)
        buttons.addView(closeBtn)

        root.addView(title)
        root.addView(statusView)
        root.addView(recycler)
        root.addView(buttons)

        runProbe(root)
        return root
    }

    private fun runProbe(anchor: View) {
        rows.clear()
        rows.add("Host Activity: ${activity?.javaClass?.name ?: "null"}")
        rows.add("Shaded OkHttp: ${OkHttpClient::class.java.name}")
        rows.add("Shaded Okio SHA-256: ${ByteString.encodeUtf8("aegires").sha256().hex().take(16)}...")
        adapter.notifyDataSetChanged()
        statusView.text = "Contacting https://discord.com/api/v9/gateway ..."

        Thread {
            val result = try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
                val req = Request.Builder()
                    .url("https://discord.com/api/v9/gateway")
                    .header("User-Agent", "Aegires-PoC")
                    .build()
                client.newCall(req).execute().use { resp ->
                    val body = resp.body()?.string()?.take(120) ?: ""
                    val proto = resp.protocol().toString()
                    val tls = resp.handshake()?.tlsVersion()?.javaName() ?: "unknown"
                    "OK HTTP ${resp.code()} ($proto, $tls): $body"
                }
            } catch (t: Throwable) {
                "FAIL ${t.javaClass.simpleName}: ${t.message}"
            }

            anchor.post {
                statusView.text = result
                rows.add("Probe: $result")
                adapter.notifyItemInserted(rows.size - 1)
            }
        }.start()
    }

    private class SimpleAdapter(private val items: List<String>) :
        RecyclerView.Adapter<SimpleAdapter.VH>() {
        class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val tv = TextView(parent.context).apply {
                textSize = 12f
                setTextColor(Color.parseColor("#DBDEE1"))
                val p = (6 * resources.displayMetrics.density).toInt()
                setPadding(0, p, 0, p)
            }
            return VH(tv)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.tv.text = "• ${items[position]}"
        }

        override fun getItemCount(): Int = items.size
    }
}
