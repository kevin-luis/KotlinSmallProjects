package com.cobacoba.mygenai

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cobacoba.mygenai.databinding.ActivityPreferencesBinding

class PreferencesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Tombol back

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(binding.settingsContainer.id, SettingsFragment())
                .commit()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
