package com.example.bobspizza

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.example.bobspizza.databinding.ActivityMainBinding
import java.io.PrintWriter
import java.lang.Double.parseDouble
import java.lang.Integer.parseInt
import kotlin.math.round

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
        } else {
            Order().process()
            return true
        }

        return super.onOptionsItemSelected(item)
    }
    
    private inner class Order {
        var orderInfo: HashMap<String, Any?> = HashMap<String, Any?>()

        /*
        Set initial state for orderInfo HashMap using values from order form.
        Reference these values instead of form values.
        */
        init {
            orderInfo[getString(R.string.radio_btn_group_delivery_method)] = getDeliveryMethod()
            orderInfo[getString(R.string.label_num_pizzas)] = getNumPizzas()
            orderInfo[getString(R.string.label_crust)] = getCrustChoice()
            orderInfo[getString(R.string.label_toppings)] = getToppings()
            orderInfo[getString(R.string.label_drinks)] = getNumDrinks()
            orderInfo[getString(R.string.label_tip)] = getTipPercentage()
        }

        /* Public method that processes the order */
        fun process() {
            calculateOrderTotals()
            displayOrderAlert()
            saveOrderToFile()
        }

        /* Gets delivery method from form */
        private fun getDeliveryMethod(): String {
            return if (binding.radioBtnPickUp.isChecked)
                getString(R.string.radio_btn_pick_up_text)
            else
                getString(R.string.radio_btn_delivery_text)
        }

        /* Gets the number of pizzas from form */
        private fun getNumPizzas(): Int {
            return parseInt(binding.inputNumPizzas.text.toString())
        }

        /* Gets the crust choice from form */
        private fun getCrustChoice(): String {
            return if (binding.radioBtnThinCrust.isChecked)
                getString(R.string.radio_btn_thin_crust_text)
            else
                getString(R.string.radio_btn_hand_tossed_text)
        }

        /* Gets the toppings from form */
        private fun getToppings(): MutableList<String?> {
            val toppings = mutableListOf<String?>()
            if (binding.checkboxMushrooms.isChecked) toppings.add(getString(R.string.checkbox_topping_mushrooms))
            if (binding.checkboxSausage.isChecked) toppings.add(getString(R.string.checkbox_topping_sausage))
            if (binding.checkboxPepperoni.isChecked) toppings.add(getString(R.string.checkbox_topping_pepperoni))
            if (binding.checkboxGreenPeppers.isChecked) toppings.add(getString(R.string.checkbox_topping_green_peppers))

            return toppings
        }

        /* Gets the number of drinks from the form */
        private fun getNumDrinks(): Int {
            return parseInt(binding.inputNumDrinks.text.toString())
        }

        /* Gets the tip percentage from the form */
        private fun getTipPercentage(): Double {
            return if (binding.radioBtnTenPercent.isChecked) 0.1
            else if (binding.radioBtnFifteenPercent.isChecked) 0.15
            else if (binding.radioBtnTwentyPercent.isChecked) 0.2
            else 0.0
        }

        /*
          Calculates the order totals using values in the orderInfo HashMap.
          Stores the calculations in the orderInfo HashMap under appropriate keys.
         */
        private fun calculateOrderTotals() {
            val numPizzas: Int =
                parseInt(orderInfo[getString(R.string.label_num_pizzas)].toString())
            val numDrinks: Int = parseInt(orderInfo[getString(R.string.label_drinks)].toString())
            val delivery: Boolean =
                when (orderInfo[getString(R.string.radio_btn_group_delivery_method)]) {
                    getString(R.string.radio_btn_delivery_text) -> true
                    else -> false
                }
            val tipPercentage: Double =
                parseDouble(orderInfo[getString(R.string.label_tip)].toString())

            val subtotal: Double = calculateSubTotal(numPizzas, numDrinks, delivery)
            orderInfo["Subtotal"] = subtotal

            val tax: Double = calculateTax(subtotal)
            orderInfo["Tax"] = tax

            val calculatedTip: Double = calculateTip(subtotal, tax, tipPercentage)
            orderInfo["Calculated Tip"] = calculatedTip

            val total: Double = calculateTotal(subtotal, tax, calculatedTip)
            orderInfo["Total"] = total

        }

        /* Calculates the order subtotal */
        private fun calculateSubTotal(numPizzas: Int, numDrinks: Int, delivery: Boolean): Double {
            val pizzaCost = numPizzas.toDouble() * 15.00
            val drinkCost = numDrinks.toDouble() * 1.5
            val deliveryCost = if (delivery) 3.0 else 0.0

            return round((pizzaCost + drinkCost + deliveryCost) * 100) / 100.0
        }

        /* Calculates the order tax */
        private fun calculateTax(subtotal: Double): Double {
            return round((subtotal * 0.1) * 100) / 100.0
        }

        /* Calculates the order tip */
        private fun calculateTip(subtotal: Double, tax: Double, tipPercentage: Double): Double {
            return round(((subtotal + tax) * tipPercentage) * 100) / 100.0
        }

        /* Calculates the order total */
        private fun calculateTotal(subtotal: Double, tax: Double, tip: Double): Double {
            return round((subtotal + tax + tip) * 100) / 100.0
        }

        /* Displays the formatted orderInfo HashMap as an AlertDialog */
        private fun displayOrderAlert() {
            val builder = AlertDialog.Builder(binding.root.context)
            builder
                .setTitle("Order Information")
                .setMessage(formattedOrderInformation())
                .setPositiveButton("Ok", null)
                .show()
        }

        /* Saves the formatted orderInfo HashMap to a file */
        private fun saveOrderToFile() {
            openFileOutput("prototypePizzaOrder.txt", MODE_PRIVATE)
                .bufferedWriter()
                .use { writer -> writer.append(formattedOrderInformation()) }
        }

        /* Formats the orderInfo HashMap for display */
        private fun formattedOrderInformation(): String {
            return """
                Order Details
                ===============
                ${getString(R.string.radio_btn_group_delivery_method)}: ${orderInfo[getString(R.string.radio_btn_group_delivery_method)]}
                ${getString(R.string.label_num_pizzas)}: ${orderInfo[getString(R.string.label_num_pizzas)]}
                ${getString(R.string.label_crust)}: ${orderInfo[getString(R.string.label_crust)]}
                ${getString(R.string.label_toppings)}: ${orderInfo[getString(R.string.label_toppings)]}
                ${getString(R.string.label_drinks)}: ${orderInfo[getString(R.string.label_drinks)]}
                ${getString(R.string.label_tip)}: ${orderInfo[getString(R.string.label_tip)]}
                ===============
                Subtotal:       ${String.format("%.2f", orderInfo["Subtotal"])}
                Tax:            ${String.format("%.2f", orderInfo["Tax"])}
                Tip:            ${String.format("%.2f", orderInfo["Calculated Tip"])}
                Total:          ${String.format("%.2f", orderInfo["Total"])}
            """.trimIndent()
        }
    }
}