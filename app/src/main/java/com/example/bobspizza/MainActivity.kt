package com.example.bobspizza

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.example.bobspizza.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()

        val preferences = getSharedPreferences(getString(R.string.pref_key), MODE_PRIVATE)
        val bgColorPreference = preferences.getString(
            getString(R.string.bg_color_key),
            getString(R.string.default_bg_color)
        )

        val bgColor = when(bgColorPreference) {
            getString(R.string.red) -> Color.parseColor("#fab1a0")
            getString(R.string.blue) -> Color.parseColor("#83ecec")
            getString(R.string.green) -> Color.parseColor("#55efc4")
            else -> Color.WHITE
        }

        binding.mainLayout.setBackgroundColor(bgColor)

        val useLargeText = preferences.getBoolean(getString(R.string.text_size_key), false)
        val textSize = if (useLargeText) 24.0f else 16.0f
        val textViewIds = listOf(
            R.id.label_background_color, R.id.label_crust, R.id.label_delivery_method,
            R.id.label_drinks, R.id.label_num_pizzas, R.id.label_tip, R.id.label_toppings
        )

        for(id in textViewIds) {
            val textview : TextView? = binding.root.findViewById(id)
            textview?.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.preferences_menu_item) {
            val intent = Intent(this, PreferencesActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}