package com.pixplicity.cryptogram.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.PuzzleProvider

class PuzzleAdapter(private val mContext: Context, private val mOnItemClickListener: OnItemClickListener) : RecyclerView.Adapter<PuzzleAdapter.ViewHolder>() {

    companion object {
        private const val TYPE_NORMAL = 0
        private const val TYPE_SELECTED = 1
    }

    private val mPuzzles: Array<Puzzle>

    private val mDarkTheme = PrefsUtils.darkTheme

    init {
        val provider = PuzzleProvider.getInstance(mContext)
        mPuzzles = provider.all
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutResId: Int
        when (viewType) {
            TYPE_NORMAL -> if (mDarkTheme) {
                layoutResId = R.layout.item_puzzle_dark
            } else {
                layoutResId = R.layout.item_puzzle
            }
            TYPE_SELECTED -> if (mDarkTheme) {
                layoutResId = R.layout.item_puzzle_selected_dark
            } else {
                layoutResId = R.layout.item_puzzle_selected
            }
            else -> if (mDarkTheme) {
                layoutResId = R.layout.item_puzzle_dark
            } else {
                layoutResId = R.layout.item_puzzle
            }
        }
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(layoutResId, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return if (PuzzleProvider.getInstance(mContext).currentIndex == position) {
            TYPE_SELECTED
        } else TYPE_NORMAL
    }

    override fun onBindViewHolder(vh: ViewHolder, position: Int) {
        val puzzle = mPuzzles[position]
        vh.position = position
        vh.tvPuzzleId!!.text = puzzle.getTitle(mContext)
        val author = puzzle.author
        if (author == null) {
            vh.tvAuthor!!.visibility = View.GONE
        } else {
            vh.tvAuthor!!.visibility = View.VISIBLE
            vh.tvAuthor!!.text = author
        }
        vh.ivCompleted!!.visibility = if (puzzle.isCompleted) View.VISIBLE else View.GONE
        vh.ivInProgress!!.visibility = if (puzzle.isInProgress) View.VISIBLE else View.GONE
    }

    override fun getItemCount(): Int {
        return mPuzzles.size
    }

    interface OnItemClickListener {

        fun onItemClick(position: Int)

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var tvPuzzleId: TextView? = null
        var tvAuthor: TextView? = null
        var ivCompleted: ImageView? = null
        var ivInProgress: ImageView? = null

        private var mPosition: Int = 0

        init {
            // FIXME find a way of using kotlin synthetics
            tvPuzzleId = itemView.findViewById(R.id.tv_puzzle_id)
            tvAuthor = itemView.findViewById(R.id.tv_author)
            ivCompleted = itemView.findViewById(R.id.iv_completed)
            ivInProgress = itemView.findViewById(R.id.iv_in_progress)

            itemView.setOnClickListener { mOnItemClickListener.onItemClick(mPosition) }
        }

        fun setPosition(position: Int) {
            mPosition = position
        }

    }

}
