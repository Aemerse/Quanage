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
import com.aemerse.quanage.constants.RNGType
import com.aemerse.quanage.persistence.HistoryDataManager
import com.aemerse.quanage.persistence.PreferencesManager
import com.aemerse.quanage.utils.*
import com.aemerse.quanage.utils.ShakeManager.Companion.get
import java.util.*

class CoinsFragment : Fragment() {
    private var focalPoint: View? = null
    private var numCoinsInput: EditText? = null
    private var resultsContainer: View? = null
    var results: TextView? = null
    private var resultsAnimationLength = 0
    private val snackbarDisplay: SnackbarDisplay = object : SnackbarDisplay {
        override fun showSnackbar(message: String?) {
            (activity as MainActivity?)!!.showSnackbar(message)
        }
    }
    private val shakeListener: ShakeManager.Listener = object: ShakeManager.Listener {
        override fun onShakeDetected(currentRngPage: Int) {
            if (currentRngPage == RNGType.COINS) {
                flip()
            }
        }
    }
    private var historyDataManager: HistoryDataManager? = null
    private val shakeManager = get()
    private var preferencesManager: PreferencesManager? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.coins_page, container, false)
        focalPoint = rootView.findViewById(R.id.focal_point)
        numCoinsInput = rootView.findViewById(R.id.num_coins)
        resultsContainer = rootView.findViewById(R.id.results_container)
        results = rootView.findViewById(R.id.results)
        resultsAnimationLength = resources.getInteger(R.integer.shorter_anim_length)
        rootView.findViewById<View>(R.id.flip).setOnClickListener { flip() }
        rootView.findViewById<View>(R.id.copy_results).setOnClickListener { copyNumbers() }
        shakeManager!!.registerListener(shakeListener)
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        historyDataManager = HistoryDataManager[activity!!]
        preferencesManager = PreferencesManager(activity!!)
        numCoinsInput!!.setText(preferencesManager!!.numCoins.toString())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveSettings()
    }

    private fun flip() {
        if (verifyForm()) {
            val mainActivity = activity as MainActivity?
            mainActivity?.playSound(RNGType.COINS)
            val numCoins = numCoinsInput!!.text.toString().toInt()
            val flips = getNumbers(
                    0,
                    1,
                    numCoins,
                    false,
                    ArrayList())
            resultsContainer!!.visibility = View.VISIBLE
            val flipText = getCoinResults(flips, activity!!)
            historyDataManager!!.addHistoryRecord(RNGType.COINS, flipText)
            animateResults(results!!, HtmlCompat.fromHtml(flipText,HtmlCompat.FROM_HTML_MODE_LEGACY), resultsAnimationLength)
        }
    }

    private fun verifyForm(): Boolean {
        hideKeyboard(activity!!)
        focalPoint!!.requestFocus()
        val numCoins = numCoinsInput!!.text.toString()
        try {
            if (numCoins.toInt() <= 0) {
                snackbarDisplay.showSnackbar(getString(R.string.zero_coins))
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
        copyResultsToClipboard(numbersText, snackbarDisplay, activity!!)
    }

    private fun saveSettings() {
        preferencesManager!!.saveNumCoins(numCoinsInput!!.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        shakeManager!!.unregisterListener(shakeListener)
        saveSettings()
    }
}