package com.notifymind.filter

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.notifymind.filter.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var adapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.get(this)

        adapter = NotificationAdapter { item, vote ->
            lifecycleScope.launch {
                db.dao().setVote(item.id, vote)
                loadData()
            }
        }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        val prefs = getSharedPreferences("notifymind", MODE_PRIVATE)
        binding.sliderThreshold.value = prefs.getFloat("threshold", 0.7f)
        binding.sliderThreshold.addOnChangeListener { _, value, _ ->
            prefs.edit().putFloat("threshold", value).apply()
            binding.txtStatus.text = "Threshold: ${(value*100).toInt()}% - reload to apply"
        }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            val recent = db.dao().recent()
            val votes = db.dao().getVotes().size
            binding.txtStatus.text = "Recent: ${recent.size} | Votes: $votes | Threshold ${(binding.sliderThreshold.value*100).toInt()}%"
            adapter.submitList(recent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }
}
