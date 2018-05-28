package com.pixplicity.cryptogram.models

import com.google.gson.annotations.SerializedName

import java.util.ArrayList

class PuzzleProgressState {

    @SerializedName("current")
    var currentId: Int? = null
        private set

    @SerializedName("progress")
    private var mProgress: ArrayList<PuzzleProgress>? = null

    val progress: ArrayList<PuzzleProgress>
        get() {
            if (mProgress == null) {
                mProgress = ArrayList()
            }
            return mProgress!!
        }

    fun setCurrentId(puzzleId: Int) {
        currentId = puzzleId
    }

    fun addProgress(puzzleProgress: PuzzleProgress) {
        progress.add(puzzleProgress)
    }

}
