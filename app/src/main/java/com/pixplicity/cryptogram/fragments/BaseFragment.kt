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

    companion object {
        /**
         * Color matrix that flips the components (`-1.0f * c + 255 = 255 - c`)
         * and keeps the alpha intact.
         */
        private val NEGATIVE = floatArrayOf(-1.0f, 0f, 0f, 0f, 255f, // red
                0f, -1.0f, 0f, 0f, 255f, // green
                0f, 0f, -1.0f, 0f, 255f, // blue
                0f, 0f, 0f, 1.0f, 0f  // alpha
        )
    }

    val isDarkTheme: Boolean
        get() {
            val activity = activity
            return (activity as? BaseActivity)?.isDarkTheme ?: false
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ButterKnife.bind(this, view)
    }

    protected fun invert(imageView: ImageView) {
        imageView.drawable.colorFilter = ColorMatrixColorFilter(NEGATIVE)
    }

    protected fun showSnackbar(text: String) {
        val activity = activity
        if (activity is BaseActivity) {
            activity.showSnackbar(text)
        }
    }

}
