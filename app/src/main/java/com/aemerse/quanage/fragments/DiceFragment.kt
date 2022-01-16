package com.aemerse.quanage.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.aemerse.quanage.R
import com.aemerse.quanage.activities.MainActivity
import com.aemerse.quanage.constants.QRNGType
import com.aemerse.quanage.persistence.HistoryDataManager
import com.aemerse.quanage.persistence.PreferencesManager
import com.aemerse.quanage.utils.*
import com.aemerse.quanage.utils.ShakeManager.Companion.get
import java.util.*

class DiceFragment : Fragment() {
    private var focalPoint: View? = null
    private var numDiceInput: EditText? = null
    private var numSidesInput: EditText? = null
    private var resultsContainer: View? = null
    var results: TextView? = null
    private var rollsPrefix: String? = null
    private var sumPrefix: String? = null
    private var resultsAnimationLength = 0
    private val snackbarDisplay: SnackbarDisplay = object : SnackbarDisplay {
        override fun showSnackbar(message: String?) {
            (activity as MainActivity?)!!.showSnackbar(message)
        }
    }
    private val shakeListener: ShakeManager.Listener = object :ShakeManager.Listener {
        override fun onShakeDetected(currentRngPage: Int) {
            if (currentRngPage == QRNGType.DICE) {
                roll()
            }
        }
    }
    private var historyDataManager: HistoryDataManager? = null
    private val shakeManager = get()
    private var preferencesManager: PreferencesManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dice_page, container, false)
        focalPoint = rootView.findViewById(R.id.focal_point)
        numDiceInput = rootView.findViewById(R.id.num_dice)
        numSidesInput = rootView.findViewById(R.id.num_sides)
        resultsContainer = rootView.findViewById(R.id.results_container)
        results = rootView.findViewById(R.id.results)
        rollsPrefix = getString(R.string.rolls_prefix)
        sumPrefix = getString(R.string.sum_prefix)
        resultsAnimationLength = resources.getInteger(R.integer.shorter_anim_length)
        shakeManager!!.registerListener(shakeListener)
        rootView.findViewById<View>(R.id.copy_results).setOnClickListener { copyNumbers() }
        rootView.findViewById<View>(R.id.roll).setOnClickListener { roll() }
        rootView.findViewById<View>(R.id.num_sides_four).setOnClickListener { loadNumSidesQuickOption(rootView.findViewById(R.id.num_sides_four)) }
        rootView.findViewById<View>(R.id.num_sides_ten).setOnClickListener { loadNumSidesQuickOption(rootView.findViewById(R.id.num_sides_ten)) }
        rootView.findViewById<View>(R.id.num_sides_six).setOnClickListener { loadNumSidesQuickOption(rootView.findViewById(R.id.num_sides_six)) }
        rootView.findViewById<View>(R.id.num_sides_twelve).setOnClickListener { loadNumSidesQuickOption(rootView.findViewById(R.id.num_sides_twelve)) }
        rootView.findViewById<View>(R.id.num_sides_eight).setOnClickListener { loadNumSidesQuickOption(rootView.findViewById(R.id.num_sides_eight)) }
        rootView.findViewById<View>(R.id.num_sides_twenty).setOnClickListener { loadNumSidesQuickOption(rootView.findViewById(R.id.num_sides_twenty)) }
        rootView.findViewById<View>(R.id.num_dice_one).setOnClickListener { loadNumDiceQuickOption(rootView.findViewById(R.id.num_dice_one)) }
        rootView.findViewById<View>(R.id.num_dice_two).setOnClickListener { loadNumDiceQuickOption(rootView.findViewById(R.id.num_dice_two)) }
        rootView.findViewById<View>(R.id.num_dice_three).setOnClickListener { loadNumDiceQuickOption(rootView.findViewById(R.id.num_dice_three)) }
        rootView.findViewById<View>(R.id.num_dice_four).setOnClickListener { loadNumDiceQuickOption(rootView.findViewById(R.id.num_dice_four)) }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        historyDataManager = HistoryDataManager[requireActivity()]
        preferencesManager = PreferencesManager(requireActivity())
        numDiceInput!!.setText(preferencesManager!!.numDice)
        numSidesInput!!.setText(preferencesManager!!.numSides)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveSettings()
    }

    private fun loadNumDiceQuickOption(view: TextView) {
        val value = view.text.toString()
        numDiceInput!!.setText(value)
    }

    private fun loadNumSidesQuickOption(view: TextView) {
        val value = view.text.toString()
        numSidesInput!!.setText(value)
    }

    fun roll() {
        if (verifyForm()) {
            val mainActivity = activity as MainActivity?
            mainActivity?.playSound(QRNGType.DICE)
            val numDice = numDiceInput!!.text.toString().toInt()
            val numSides = numSidesInput!!.text.toString().toInt()
            val rolls = getNumbers(
                    1,
                    numSides,
                    numDice,
                    false,
                    ArrayList())
            resultsContainer!!.visibility = View.VISIBLE
            val rollsText = getDiceResults(rolls, rollsPrefix, sumPrefix)
            historyDataManager!!.addHistoryRecord(QRNGType.DICE, rollsText)
            animateResults(results!!, HtmlCompat.fromHtml(rollsText,HtmlCompat.FROM_HTML_MODE_LEGACY), resultsAnimationLength)
        }
    }

    private fun verifyForm(): Boolean {
        hideKeyboard(requireActivity())
        focalPoint!!.requestFocus()
        val numSides = numSidesInput!!.text.toString()
        val numDice = numDiceInput!!.text.toString()
        try {
            if (numSides.toInt() <= 0) {
                snackbarDisplay.showSnackbar(getString(R.string.zero_sides))
                return false
            } else if (numDice.toInt() <= 0) {
                snackbarDisplay.showSnackbar(getString(R.string.zero_dice))
                return false
            }
        } catch (exception: NumberFormatException) {
            snackbarDisplay.showSnackbar(getString(R.string.not_a_number))
            return false
        }
        return true
    }

    private fun copyNumbers() {
        val numbersText = results!!.text.toString()
        copyResultsToClipboard(numbersText, snackbarDisplay, requireActivity())
    }

    private fun saveSettings() {
        preferencesManager!!.saveDiceSettings(
                numSidesInput!!.text.toString(),
                numDiceInput!!.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shakeManager!!.unregisterListener(shakeListener)
        saveSettings()
    }
}