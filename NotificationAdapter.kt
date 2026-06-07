package com.notifymind.filter

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.notifymind.filter.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.get(this)

        binding.btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        binding.btnTrain.setOnClickListener {
            lifecycleScope.launch {
                val recent = db.dao().recent()
                binding.txtStatus.text = "Loaded ${recent.size} notifications. Vote on them to train."
            }
        }
        // Simple UI: list recent notifications with Keep/Dismiss buttons
        // Full RecyclerView implementation omitted for brevity - add in Android Studio
    }
}
