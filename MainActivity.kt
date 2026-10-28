package com.example.dexgamepadfix

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private val prefs by lazy { getSharedPreferences("dexfix", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        findViewById<Button>(R.id.permission).setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
        }
        findViewById<Button>(R.id.enable).setOnClickListener { enableFix() }
        findViewById<Button>(R.id.disable).setOnClickListener { disableFix() }
        refresh()
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun enableFix() {
        if (!Settings.System.canWrite(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:$packageName")))
            return
        }
        val oldTimeout = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, 30000)
        if (!prefs.getBoolean("active", false)) prefs.edit().putInt("old_timeout", oldTimeout).apply()
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, Int.MAX_VALUE)
        prefs.edit().putBoolean("active", true).apply()
        refresh()
    }

    private fun disableFix() {
        if (!Settings.System.canWrite(this)) return
        val old = prefs.getInt("old_timeout", 30000)
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, old)
        prefs.edit().putBoolean("active", false).apply()
        refresh()
    }

    private fun refresh() {
        val active = prefs.getBoolean("active", false)
        val timeout = Settings.System.getInt(contentResolver, Settings.System.SCREEN_OFF_TIMEOUT, -1)
        status.text = "Fix: ${if (active) "ON" else "OFF"}\nWRITE_SETTINGS: ${if (Settings.System.canWrite(this)) "OK" else "нет"}\nScreen timeout: $timeout ms"
    }
}
