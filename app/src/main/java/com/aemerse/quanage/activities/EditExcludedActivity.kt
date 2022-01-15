package com.aemerse.quanage.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.aemerse.quanage.R
import com.aemerse.quanage.adapters.ExcludedNumbersAdapter
import com.aemerse.quanage.databinding.EditExcludedBinding
import com.aemerse.quanage.utils.showSnackbar
import com.joanzapata.iconify.IconDrawable
import com.joanzapata.iconify.fonts.IoniconsIcons

class EditExcludedActivity : AppCompatActivity() {

    private var adapter: ExcludedNumbersAdapter? = null
    private var minimum = 0
    private var maximum = 0

    private lateinit var binding: EditExcludedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = EditExcludedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.addIcon.setImageDrawable(IconDrawable(this, IoniconsIcons.ion_android_add)
                .colorRes(R.color.white)
                .actionBarSize())
        minimum = intent.getIntExtra(MINIMUM_KEY, 0)
        maximum = intent.getIntExtra(MAXIMUM_KEY, 0)
        val excludedNumbers = intent.getIntegerArrayListExtra(EXCLUDED_NUMBERS_KEY)
        adapter = ExcludedNumbersAdapter(excludedNumbers!!, binding.noExcluded)
        binding.excludedList.adapter = adapter
        findViewById<View>(R.id.add_excluded).setOnClickListener {
            val enteredExcluded = binding.excludedInput.text.toString()
            binding.excludedInput.setText("")
            try {
                if (enteredExcluded.isEmpty()) {
                    showSnackbar(binding.root, getString(R.string.not_a_number), this)
                } else if (enteredExcluded.toInt() > maximum || enteredExcluded.toInt() < minimum) {
                    val range = "($minimum to $maximum)"
                    showSnackbar(binding.root, getString(R.string.not_in_range) + range, this)
                } else if (adapter!!.containsNumber(enteredExcluded.toInt())!!) {
                    showSnackbar(binding.root, getString(R.string.already_excluded), this)
                } else {
                    adapter!!.addNumber(enteredExcluded.toInt())
                }
            }
            catch (exception: NumberFormatException) {
                showSnackbar(binding.root, getString(R.string.not_a_number), this)
            }
        }
        findViewById<View>(R.id.submit).setOnClickListener { finish() }
    }

    override fun finish() {
        val returnIntent = Intent()
        returnIntent.putIntegerArrayListExtra(EXCLUDED_NUMBERS_KEY, adapter!!.excludedNumbers)
        setResult(RESULT_OK, returnIntent)
        super.finish()
    }

    companion object {
        const val EXCLUDED_NUMBERS_KEY = "excludedNumbers"
        const val MINIMUM_KEY = "minimum"
        const val MAXIMUM_KEY = "maximum"
    }
}