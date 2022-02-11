package com.example.bobspizza

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.RadioGroup
import com.example.bobspizza.databinding.ActivityPreferencesBinding

class PreferencesActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener,
    RadioGroup.OnCheckedChangeListener {
    private lateinit var binding: ActivityPreferencesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreferencesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.largeTextSwitch.setOnCheckedChangeListener(this)
        binding.bgColorRadioGroup.setOnCheckedChangeListener(this)
    }

    override fun onStart() {
        super.onStart()

        val preferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)

        val useLargeText = preferences.getBoolean(getString(R.string.text_size_key), false)
        binding.largeTextSwitch.isChecked = useLargeText

        val bg_color = preferences.getString(
            getString(R.string.bg_color_key),
            getString(R.string.default_bg_color)
        )

        when(bg_color) {
            getString(R.string.red) -> binding.radioOptionRed.isChecked = true
            getString(R.string.blue) -> binding.radioOptionBlue.isChecked = true
            getString(R.string.green) -> binding.radioOptionGreen.isChecked = true
            else -> binding.radioOptionDefaultBgColor.isChecked = true
        }
    }

    override fun onCheckedChanged(button: CompoundButton?, checked: Boolean) {
        val preferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)

        with(preferences.edit()) {
            putBoolean(getString(R.string.text_size_key), checked)
            apply()
        }
    }

    override fun onCheckedChanged(radioGroup: RadioGroup, buttonId: Int) {
        val preferences = getSharedPreferences(getString(R.string.pref_key), Context.MODE_PRIVATE)

        with(preferences.edit()) {
            var color = when(buttonId) {
                R.id.radio_option_red -> getString(R.string.red)
                R.id.radio_option_blue -> getString(R.string.blue)
                R.id.radio_option_green -> getString(R.string.green)
                else -> getString(R.string.default_bg_color)
            }
            putString(getString(R.string.bg_color_key), color)
            apply()
        }
    }
}