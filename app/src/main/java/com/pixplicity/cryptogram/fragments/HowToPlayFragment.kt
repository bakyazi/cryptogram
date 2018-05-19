package com.pixplicity.cryptogram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.HtmlCompat
import com.pixplicity.cryptogram.utils.VideoUtils
import kotlinx.android.synthetic.main.fragment_how_to_play.*


class HowToPlayFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_how_to_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDarkTheme) {
            invert(iv_instructions1)
            invert(iv_instructions2)
        }

        tv_how_to_play_1.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_1))
        tv_how_to_play_2.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_2))
        tv_how_to_play_3.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_3))
        tv_how_to_play_4.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_4))

        VideoUtils.setup(activity!!, rf_video1, VideoUtils.VIDEO_INSTRUCTION)
    }

}
