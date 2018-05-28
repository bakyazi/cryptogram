package com.pixplicity.cryptogram.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R

class HowToPlayActivity : BaseActivity() {

    companion object {
        fun create(context: Context): Intent {
            return Intent(context, HowToPlayActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_play)

        mToolbar.setTitle(R.string.how_to_play)

        setHomeButtonEnabled(true)

        CryptogramApp.instance.firebaseAnalytics.setCurrentScreen(this, CryptogramApp.CONTENT_HOW_TO_PLAY, null)
        Answers.getInstance().logContentView(ContentViewEvent().putContentName(CryptogramApp.CONTENT_HOW_TO_PLAY))
    }

}
