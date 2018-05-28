package com.pixplicity.cryptogram.utils

import android.os.Handler
import android.os.Looper

import com.squareup.otto.Bus

object EventProvider {

    private val handler = Handler(Looper.getMainLooper())
    val bus = Bus()

    fun postEvent(event: Any) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            bus.post(event)
        } else {
            postEventDelayed(event)
        }
    }

    fun postEventDelayed(event: Any): Boolean {
        return handler.post { bus.post(event) }
    }

    fun postEventDelayed(event: Any, delayMillis: Long): Boolean {
        return handler.postDelayed({ bus.post(event) }, delayMillis)
    }

}
