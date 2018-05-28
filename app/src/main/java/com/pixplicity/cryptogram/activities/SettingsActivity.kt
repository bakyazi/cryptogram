package com.pixplicity.cryptogram.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R

class SettingsActivity : BaseActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mToolbar.setTitle(R.string.settings)

        setHomeButtonEnabled(true)

        CryptogramApp.instance.firebaseAnalytics.setCurrentScreen(this, CryptogramApp.CONTENT_SETTINGS, null)
        Answers.getInstance().logContentView(ContentViewEvent().putContentName(CryptogramApp.CONTENT_SETTINGS))
    }

}
