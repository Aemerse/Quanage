package com.aemerse.quanage.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.aemerse.quanage.fragments.CoinsFragment
import com.aemerse.quanage.fragments.DiceFragment
import com.aemerse.quanage.fragments.QRNGFragment

class HomepageTabsAdapter(fragmentManager: FragmentManager?, private val tabNames: Array<String>) : FragmentStatePagerAdapter(fragmentManager!!) {
    private var rngFragment: QRNGFragment? = null
    private var diceFragment: DiceFragment? = null
    private var coinsFragment: CoinsFragment? = null
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                if (rngFragment == null) {
                    rngFragment = QRNGFragment()
                }
                rngFragment!!
            }
            1 -> {
                if (diceFragment == null) {
                    diceFragment = DiceFragment()
                }
                diceFragment!!
            }
            else -> {
                if (coinsFragment == null) {
                    coinsFragment = CoinsFragment()
                }
                coinsFragment!!
            }
        }
    }

    override fun getCount(): Int {
        return tabNames.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return tabNames[position]
    }
}