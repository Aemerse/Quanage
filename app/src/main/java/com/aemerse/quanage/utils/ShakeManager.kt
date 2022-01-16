package com.aemerse.quanage.utils

import com.aemerse.quanage.constants.QRNGType
import java.util.concurrent.CopyOnWriteArraySet

/**
 * Utility class to broadcast shake detected signals because I don't get fragments.
 */
class ShakeManager {
    interface Listener {
        fun onShakeDetected(@QRNGType currentRngPage: Int)
    }

    private val listeners: CopyOnWriteArraySet<Listener> = CopyOnWriteArraySet()
    fun registerListener(listener: Listener) {
        listeners.add(listener)
    }

    fun unregisterListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun onShakeDetected(@QRNGType currentRngPage: Int) {
        for (listener in listeners) {
            listener.onShakeDetected(currentRngPage)
        }
    }

    companion object {
        private var instance: ShakeManager? = null
        @JvmStatic
        fun get(): ShakeManager? {
            if (instance == null) {
                instance = sync
            }
            return instance
        }

        @get:Synchronized
        private val sync: ShakeManager?
            get() {
                if (instance == null) {
                    instance = ShakeManager()
                }
                return instance
            }
    }

}