package com.pixplicity.cryptogram.fragments

import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.View
import android.widget.ImageView

import com.pixplicity.cryptogram.activities.BaseActivity

import butterknife.ButterKnife

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
