package com.aemerse.quanage.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.aemerse.quanage.R
import com.aemerse.quanage.activities.EditExcludedActivity
import com.aemerse.quanage.activities.MainActivity
import com.aemerse.quanage.constants.QRNGType
import com.aemerse.quanage.model.QRNGSettings
import com.aemerse.quanage.model.RNGSettingsViewHolder
import com.aemerse.quanage.persistence.HistoryDataManager
import com.aemerse.quanage.persistence.PreferencesManager
import com.aemerse.quanage.utils.*
import java.util.*

class QRNGFragment : Fragment() {
    private var focalPoint: View? = null
    private var minimumInput: EditText? = null
    private var maximumInput: EditText? = null
    private var quantityInput: EditText? = null
    private var excludedNumsDisplay: TextView? = null
    private var resultsContainer: View? = null
    var results: TextView? = null
    private var numbersPrefix: String? = null
    private var sumPrefix: String? = null
    private var noExcludedNumbers: String? = null
    var blue = 0
    private var resultsAnimationLength = 0
    private val snackbarDisplay: SnackbarDisplay = object : SnackbarDisplay {
        override fun showSnackbar(message: String?) {
            (activity as MainActivity?)!!.showSnackbar(message)
        }
    }
    private val shakeListener: ShakeManager.Listener = object: ShakeManager.Listener {
        override fun onShakeDetected(currentRngPage: Int) {
            if (currentRngPage == QRNGType.NUMBER) {
                generate()
            }
        }
    }
    private var preferencesManager: PreferencesManager? = null
    private var QRNGSettings: QRNGSettings? = null
    private var settingsDialog: MaterialDialog? = null
    private var excludedDialog: MaterialDialog? = null
    private var moreSettingsViewHolder: RNGSettingsViewHolder? = null
    private var historyDataManager: HistoryDataManager? = null
    private val shakeManager = ShakeManager.get()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.rng_page, container, false)
        focalPoint = rootView.findViewById(R.id.focal_point)
        minimumInput = rootView.findViewById(R.id.minimum)
        maximumInput = rootView.findViewById(R.id.maximum)
        quantityInput = rootView.findViewById(R.id.quantity)
        excludedNumsDisplay = rootView.findViewById(R.id.excluded_numbers)
        resultsContainer = rootView.findViewById(R.id.results_container)
        results = rootView.findViewById(R.id.results)
        numbersPrefix = getString(R.string.numbers_prefix)
        sumPrefix = getString(R.string.sum_prefix)
        noExcludedNumbers = getString(R.string.no_excluded_numbers)
        blue = resources.getColor(R.color.blue)
        resultsAnimationLength = resources.getInteger(R.integer.shorter_anim_length)
        rootView.findViewById<View>(R.id.excluded_numbers_container).setOnClickListener { editExcluded() }
        rootView.findViewById<View>(R.id.excluded_numbers).setOnClickListener { editExcluded() }
        rootView.findViewById<View>(R.id.rng_settings).setOnClickListener { showRNGSettings() }
        rootView.findViewById<View>(R.id.generate).setOnClickListener { generate() }
        rootView.findViewById<View>(R.id.copy_results).setOnClickListener { copyNumbers() }
        minimumInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                minChanged()
            }
        })
        maximumInput!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                maxChanged()
            }
        })
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        historyDataManager = HistoryDataManager[requireActivity()]
        preferencesManager = PreferencesManager(requireActivity())
        QRNGSettings = preferencesManager!!.rNGSettings

        // Setting the value of min/max clears the excluded numbers, so we have to save them
        val excludedCopy = ArrayList(QRNGSettings!!.excludedNumbers!!)
        minimumInput!!.setText(java.lang.String.valueOf(QRNGSettings!!.minimum))
        maximumInput!!.setText(java.lang.String.valueOf(QRNGSettings!!.maximum))
        QRNGSettings!!.excludedNumbers = excludedCopy
        setSettingsDialog()
        setExcludedDialog()
        quantityInput!!.setText(java.lang.String.valueOf(QRNGSettings!!.numNumbers))
        loadExcludedNumbers()
        focalPoint!!.requestFocus()
        shakeManager!!.registerListener(shakeListener)
    }

    private fun setSettingsDialog() {
        settingsDialog = MaterialDialog.Builder(requireActivity())
                .theme(Theme.DARK)
                .title(R.string.rng_settings)
                .customView(R.layout.rng_settings, true)
                .positiveText(android.R.string.yes)
                .onPositive { dialog: MaterialDialog?, which: DialogAction? -> loadExcludedNumbers() }
                .build()
        moreSettingsViewHolder = RNGSettingsViewHolder(settingsDialog!!.customView!!, requireActivity(), QRNGSettings!!)
    }

    private fun setExcludedDialog() {
        val excludedNumbers = QRNGSettings!!.excludedNumbers
        excludedDialog = MaterialDialog.Builder(requireActivity())
                .theme(Theme.DARK)
                .title(R.string.excluded_numbers)
                .content(getExcludedList(excludedNumbers!!, noExcludedNumbers!!))
                .positiveText(android.R.string.yes)
                .negativeText(R.string.edit)
                .onAny { dialog: MaterialDialog?, which: DialogAction ->
                    if (which == DialogAction.NEGATIVE) {
                        editExcludedNumbers()
                    } else if (which == DialogAction.NEUTRAL) {
                        QRNGSettings!!.excludedNumbers!!.clear()
                        loadExcludedNumbers()
                        snackbarDisplay.showSnackbar(getString(R.string.excluded_clear))
                    }
                }
                .build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        saveRNGSettings()
    }

    private fun saveRNGSettings() {
        try {
            QRNGSettings!!.minimum = minimumInput!!.text.toString().toInt()
        } catch (ignored: NumberFormatException) {
        }
        try {
            QRNGSettings!!.maximum = maximumInput!!.text.toString().toInt()
        } catch (ignored: NumberFormatException) {
        }
        try {
            QRNGSettings!!.numNumbers = quantityInput!!.text.toString().toInt()
        } catch (ignored: NumberFormatException) {
        }
        QRNGSettings!!.sortType = moreSettingsViewHolder!!.sortIndex
        QRNGSettings!!.isNoDupes = moreSettingsViewHolder!!.noDupes
        QRNGSettings!!.isShowSum = moreSettingsViewHolder!!.showSum()
        QRNGSettings!!.isHideExcluded = moreSettingsViewHolder!!.hideExcludes()
        preferencesManager!!.saveRNGSettings(QRNGSettings!!)
    }

    private fun loadExcludedNumbers() {
        val excludedNumbers = QRNGSettings!!.excludedNumbers
        if (excludedNumbers!!.isEmpty()) {
            excludedNumsDisplay!!.setText(R.string.none)
        } else {
            if (moreSettingsViewHolder!!.hideExcludes()) {
                excludedNumsDisplay!!.setText(R.string.ellipsis)
            } else {
                excludedNumsDisplay!!.text = getExcludedList(excludedNumbers, noExcludedNumbers!!)
            }
        }
    }

    private fun editExcluded() {
        val excludedNumbers = QRNGSettings!!.excludedNumbers
        excludedDialog!!.setContent(getExcludedList(excludedNumbers!!, noExcludedNumbers!!))
        if (!excludedNumbers.isEmpty()) {
            excludedDialog!!.setActionButton(DialogAction.NEUTRAL, R.string.clear)
        }
        excludedDialog!!.show()
    }

    private fun editExcludedNumbers() {
        try {
            val intent = Intent(activity, EditExcludedActivity::class.java)
            intent.putExtra(EditExcludedActivity.MINIMUM_KEY, minimumInput!!.text.toString().toInt())
            intent.putExtra(EditExcludedActivity.MAXIMUM_KEY, maximumInput!!.text.toString().toInt())
            intent.putIntegerArrayListExtra(
                    EditExcludedActivity.EXCLUDED_NUMBERS_KEY,
                    QRNGSettings!!.excludedNumbers)
            startActivityForResult(intent, 1)
            requireActivity().overridePendingTransition(R.anim.slide_left_out, R.anim.slide_left_in)
        } catch (exception: NumberFormatException) {
            snackbarDisplay.showSnackbar(getString(R.string.not_a_number))
        }
    }

    private fun showRNGSettings() {
        settingsDialog!!.show()
    }

    fun minChanged() {
        QRNGSettings!!.excludedNumbers!!.clear()
        loadExcludedNumbers()
    }

    fun maxChanged() {
        QRNGSettings!!.excludedNumbers!!.clear()
        loadExcludedNumbers()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            QRNGSettings!!.excludedNumbers = data!!.getIntegerArrayListExtra(
                    EditExcludedActivity.EXCLUDED_NUMBERS_KEY)
            loadExcludedNumbers()
            snackbarDisplay.showSnackbar(getString(R.string.excluded_updated))
        }
    }

    fun generate() {
        if (verifyForm()) {
            val mainActivity = activity as MainActivity?
            mainActivity?.playSound(QRNGType.NUMBER)
            val minimum = minimumInput!!.text.toString().toInt()
            val maximum = maximumInput!!.text.toString().toInt()
            val quantity = quantityInput!!.text.toString().toInt()
            val generatedNums: List<Int> = getNumbers(minimum, maximum, quantity, moreSettingsViewHolder!!.noDupes, QRNGSettings!!.excludedNumbers)
            when (moreSettingsViewHolder!!.sortIndex) {
                1 -> Collections.sort(generatedNums)
                2 -> {
                    Collections.sort(generatedNums)
                    Collections.reverse(generatedNums)
                }
            }
            resultsContainer!!.visibility = View.VISIBLE
            val resultsString = getResultsString(
                    generatedNums,
                    moreSettingsViewHolder!!.showSum(),
                    numbersPrefix,
                    sumPrefix)
            historyDataManager!!.addHistoryRecord(QRNGType.NUMBER, resultsString)
            animateResults(results!!, Html.fromHtml(resultsString), resultsAnimationLength)
        }
    }

    private fun verifyForm(): Boolean {
        hideKeyboard(requireActivity())
        focalPoint!!.requestFocus()
        val minimum = minimumInput!!.text.toString()
        val maximum = maximumInput!!.text.toString()
        val quantity = quantityInput!!.text.toString()
        try {
            val numAvailable = maximum.toInt() - minimum.toInt() + 1
            val quantityRestriction = if (moreSettingsViewHolder!!.noDupes) quantity.toInt() else 1
            if (minimum.isEmpty() || maximum.isEmpty() || quantity.isEmpty()) {
                snackbarDisplay.showSnackbar(getString(R.string.missing_input))
                return false
            } else if (maximum.toInt() < minimum.toInt()) {
                snackbarDisplay.showSnackbar(getString(R.string.bigger_min))
                return false
            } else if (quantity.toInt() <= 0) {
                snackbarDisplay.showSnackbar(getString(R.string.non_zero_quantity))
                return false
            } else if (numAvailable < quantityRestriction + QRNGSettings!!.excludedNumbers!!.size) {
                snackbarDisplay.showSnackbar(getString(R.string.overlimited_range))
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

    override fun onDestroyView() {
        super.onDestroyView()
        shakeManager!!.unregisterListener(shakeListener)
        saveRNGSettings()
    }
}