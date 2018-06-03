package com.pixplicity.cryptogram.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R

class DonateActivity : BaseActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, DonateActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        CryptogramApp.instance!!.firebaseAnalytics.setCurrentScreen(this, CryptogramApp.CONTENT_DONATE, null)
        Answers.getInstance().logContentView(ContentViewEvent().putContentName(CryptogramApp.CONTENT_DONATE))
    }

}
