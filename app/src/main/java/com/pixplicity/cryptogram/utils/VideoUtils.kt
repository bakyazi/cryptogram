package com.pixplicity.cryptogram.utils

import android.app.Activity
import android.graphics.Color
import android.net.Uri
import android.support.annotation.DrawableRes
import android.widget.ImageView

import com.afollestad.easyvideoplayer.EasyVideoCallback
import com.afollestad.easyvideoplayer.EasyVideoPlayer
import com.pixplicity.cryptogram.R

import net.soulwolf.widget.ratiolayout.RatioDatumMode
import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout

object VideoUtils {

    val VIDEO_INSTRUCTION = Video("vid_intro1", R.drawable.im_vid_intro1, 926f, 166f)
    val VIDEO_HELP = Video("vid_help", R.drawable.im_vid_help, 720f, 360f)

    class Video(val videoResName: String,
                @param:DrawableRes val stillFrameResId: Int,
                val width: Float, val height: Float)

    fun setup(activity: Activity,
              ratioFrameLayout: RatioFrameLayout,
              video: Video): EasyVideoPlayer? {
        ratioFrameLayout.setRatio(RatioDatumMode.DATUM_WIDTH, video.width, video.height)
        val player = ratioFrameLayout.findViewById<EasyVideoPlayer>(R.id.player)
        if (player != null) {
            player.disableControls()
            player.setBackgroundColor(Color.WHITE)
            player.setCallback(object : EasyVideoCallback {
                override fun onStarted(player: EasyVideoPlayer) {}

                override fun onPaused(player: EasyVideoPlayer) {}

                override fun onPreparing(player: EasyVideoPlayer) {}

                override fun onPrepared(player: EasyVideoPlayer) {}

                override fun onBuffering(percent: Int) {}

                override fun onError(player: EasyVideoPlayer, e: Exception) {}

                override fun onCompletion(player: EasyVideoPlayer) {
                    player.seekTo(0)
                    player.start()
                }

                override fun onRetry(player: EasyVideoPlayer, source: Uri) {}

                override fun onSubmit(player: EasyVideoPlayer, source: Uri) {}
            })
            player.setAutoPlay(true)

            val uri = Uri.parse("android.resource://" + activity.packageName + "/raw/" + video.videoResName)
            player.setSource(uri)
        } else {
            val ivVideo = ratioFrameLayout.findViewById<ImageView>(R.id.iv_still_frame)
            ivVideo.setImageResource(video.stillFrameResId)
        }
        return player
    }

}
