package com.pixplicity.cryptogram.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import butterknife.ButterKnife
import com.pixplicity.cryptogram.activities.BaseActivity

abstract class BaseFragment : Fragment() {

    val isDarkTheme: Boolean
        get() {
            val activity = activity
            return (activity as? BaseActivity)?.isDarkTheme ?: false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
    }

    protected fun showSnackbar(text: String) {
        val activity = activity
        if (activity is BaseActivity) {
            activity.showSnackbar(text)
        }
    }

}
