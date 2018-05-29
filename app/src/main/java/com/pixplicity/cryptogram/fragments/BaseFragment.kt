package com.pixplicity.cryptogram.fragments

import android.support.v4.app.Fragment
import com.pixplicity.cryptogram.activities.BaseActivity

abstract class BaseFragment : Fragment() {

    val isDarkTheme: Boolean
        get() {
            val activity = activity
            return (activity as? BaseActivity)?.isDarkTheme ?: false
        }

}
