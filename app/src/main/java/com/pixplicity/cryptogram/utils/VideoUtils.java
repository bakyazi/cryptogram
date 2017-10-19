package com.pixplicity.cryptogram.utils;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.afollestad.easyvideoplayer.EasyVideoCallback;
import com.afollestad.easyvideoplayer.EasyVideoPlayer;
import com.pixplicity.cryptogram.R;

import net.soulwolf.widget.ratiolayout.RatioDatumMode;
import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout;

public class VideoUtils {

    public static class Video {

        private final String mVideoResName;
        private final int mStillFrameResId;
        private final float mWidth;
        private final float mHeight;

        public Video(String videoResName,
                     @DrawableRes int stillFrameResId,
                     float width, float height) {
            mVideoResName = videoResName;
            mStillFrameResId = stillFrameResId;
            mWidth = width;
            mHeight = height;
        }

        public float getWidth() {
            return mWidth;
        }

        public float getHeight() {
            return mHeight;
        }

        public int getStillFrameResId() {
            return mStillFrameResId;
        }

        public String getVideoResName() {
            return mVideoResName;
        }

    }

    public static final Video VIDEO_INSTRUCTION = new Video("vid_intro1", R.drawable.im_vid_intro1, 926, 166);
    public static final Video VIDEO_HELP = new Video("vid_help", R.drawable.im_vid_help, 720, 360);

    @Nullable
    public static EasyVideoPlayer setup(@NonNull Activity activity,
                                        @NonNull RatioFrameLayout ratioFrameLayout,
                                        @NonNull Video video) {
        ratioFrameLayout.setRatio(RatioDatumMode.DATUM_WIDTH, video.getWidth(), video.getHeight());
        final EasyVideoPlayer player = ratioFrameLayout.findViewById(R.id.player);
        if (player != null) {
            player.disableControls();
            player.setBackgroundColor(Color.WHITE);
            player.setCallback(new EasyVideoCallback() {
                @Override
                public void onStarted(EasyVideoPlayer player) {
                }

                @Override
                public void onPaused(EasyVideoPlayer player) {
                }

                @Override
                public void onPreparing(EasyVideoPlayer player) {
                }

                @Override
                public void onPrepared(EasyVideoPlayer player) {
                }

                @Override
                public void onBuffering(int percent) {
                }

                @Override
                public void onError(EasyVideoPlayer player, Exception e) {
                }

                @Override
                public void onCompletion(EasyVideoPlayer player) {
                    player.seekTo(0);
                    player.start();
                }

                @Override
                public void onRetry(EasyVideoPlayer player, Uri source) {
                }

                @Override
                public void onSubmit(EasyVideoPlayer player, Uri source) {
                }
            });
            player.setAutoPlay(true);

            Uri uri = Uri.parse("android.resource://" + activity.getPackageName() + "/raw/" + video.getVideoResName());
            player.setSource(uri);
        } else {
            ImageView ivVideo = ratioFrameLayout.findViewById(R.id.iv_still_frame);
            ivVideo.setImageResource(video.getStillFrameResId());
        }
        return player;
    }

}
