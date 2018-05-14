package com.pixplicity.cryptogram.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.HtmlCompat
import com.pixplicity.cryptogram.utils.VideoUtils

import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout

import butterknife.BindView


class HowToPlayFragment : BaseFragment() {

    companion object {
        private val TAG = HowToPlayFragment::class.java.simpleName
    }

    @BindView(R.id.tv_how_to_play_1)
    var mTvHowToPlay1: TextView? = null

    @BindView(R.id.tv_how_to_play_2)
    var mTvHowToPlay2: TextView? = null

    @BindView(R.id.tv_how_to_play_3)
    var mTvHowToPlay3: TextView? = null

    @BindView(R.id.tv_how_to_play_4)
    var mTvHowToPlay4: TextView? = null

    @BindView(R.id.iv_instructions1)
    var mIvInstructions1: ImageView? = null

    @BindView(R.id.iv_instructions2)
    var mIvInstructions2: ImageView? = null

    @BindView(R.id.rf_video1)
    var mRfVideo1: RatioFrameLayout? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_how_to_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDarkTheme) {
            invert(mIvInstructions1!!)
            invert(mIvInstructions2!!)
        }

        mTvHowToPlay1!!.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_1))
        mTvHowToPlay2!!.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_2))
        mTvHowToPlay3!!.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_3))
        mTvHowToPlay4!!.text = HtmlCompat.fromHtml(getString(R.string.how_to_play_4))

        VideoUtils.setup(activity!!, mRfVideo1!!, VideoUtils.VIDEO_INSTRUCTION)
    }

}
