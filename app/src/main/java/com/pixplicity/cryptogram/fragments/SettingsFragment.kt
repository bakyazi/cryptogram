package com.pixplicity.cryptogram.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.RadioButton

import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.activities.BaseActivity
import com.pixplicity.cryptogram.activities.CryptogramActivity
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.PuzzleProvider
import com.pixplicity.cryptogram.utils.StyleUtils

import java.util.Locale

import butterknife.BindView
import butterknife.OnClick


class SettingsFragment : BaseFragment() {

    @BindView(R.id.vg_content)
    var mVgContent: ViewGroup? = null

    @BindView(R.id.vg_busy)
    var mVgBusy: ViewGroup? = null

    @BindView(R.id.rb_theme_light)
    var mRbThemeLight: RadioButton? = null

    @BindView(R.id.rb_theme_dark)
    var mRbThemeDark: RadioButton? = null

    @BindView(R.id.rb_text_size_small)
    var mRbTextSizeSmall: RadioButton? = null

    @BindView(R.id.rb_text_size_normal)
    var mRbTextSizeNormal: RadioButton? = null

    @BindView(R.id.rb_text_size_large)
    var mRbTextSizeLarge: RadioButton? = null

    @BindView(R.id.cb_show_topic)
    var mCbShowTopic: CheckBox? = null

    @BindView(R.id.cb_show_score)
    var mCbShowScore: CheckBox? = null

    @BindView(R.id.rb_keyboard_builtin)
    var mRbKeyboardBuiltin: RadioButton? = null

    @BindView(R.id.rb_keyboard_system)
    var mRbKeyboardSystem: RadioButton? = null

    @BindView(R.id.cb_show_hints)
    var mCbShowHints: CheckBox? = null

    @BindView(R.id.cb_auto_advance)
    var mCbAutoAdvance: CheckBox? = null

    @BindView(R.id.cb_skip_filled_cells)
    var mCbSkipFilledCells: CheckBox? = null

    @BindView(R.id.cb_randomize)
    var mCbRandomize: CheckBox? = null

    @BindView(R.id.bt_reset_dialogs)
    var mBtResetDialogs: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_settings, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        update()
    }

    @OnClick(R.id.iv_theme_light)
    fun onClickIvThemeLight() {
        mRbThemeLight!!.isChecked = true
    }

    @OnClick(R.id.iv_theme_dark)
    fun onClickIvThemeDark() {
        mRbThemeDark!!.isChecked = true
    }

    private fun update() {
        // Theme settings
        updateCompoundButton(mRbThemeLight!!, !PrefsUtils.getDarkTheme(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTheme(false)
            }
        })
        updateCompoundButton(mRbThemeDark!!, PrefsUtils.getDarkTheme(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTheme(true)
            }
        })

        // Display settings
        updateCompoundButton(mRbTextSizeSmall!!, PrefsUtils.getTextSize() == -1, CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTextSize(-1)
            }
        })
        updateCompoundButton(mRbTextSizeNormal!!, PrefsUtils.getTextSize() == 0, CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTextSize(0)
            }
        })
        updateCompoundButton(mRbTextSizeLarge!!, PrefsUtils.getTextSize() == 1, CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTextSize(1)
            }
        })
        updateCompoundButton(mCbShowTopic!!, PrefsUtils.getShowTopic(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setShowTopic(checked) })
        updateCompoundButton(mCbShowScore!!, PrefsUtils.getShowScore(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setShowScore(checked) })


        // Keyboard settings
        updateCompoundButton(mRbKeyboardBuiltin!!, !PrefsUtils.getUseSystemKeyboard(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setUseSystemKeyboard(false)
            }
        })
        updateCompoundButton(mRbKeyboardSystem!!, PrefsUtils.getUseSystemKeyboard(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                // Show warning that it works for shit
                MaterialDialog.Builder(activity!!)
                        .content(R.string.keyboard_system_dialog)
                        .positiveText(R.string.keyboard_system_dialog_ok)
                        .dismissListener { setUseSystemKeyboard(true) }
                        .show()
            }
        })
        updateCompoundButton(mCbShowHints!!, PrefsUtils.getShowUsedChars(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setShowUsedChars(checked) })
        updateCompoundButton(mCbAutoAdvance!!, PrefsUtils.getAutoAdvance(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setAutoAdvance(checked) })
        updateCompoundButton(mCbSkipFilledCells!!, PrefsUtils.getSkipFilledCells(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setSkipFilledCells(checked) })

        // Other settings
        updateCompoundButton(mCbRandomize!!, PrefsUtils.getRandomize(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setRandomize(checked) })

        mBtResetDialogs!!.isEnabled = PrefsUtils.getNeverAskRevealLetter() || PrefsUtils.getNeverAskRevealMistakes()
    }

    private fun setTextSize(textSize: Int) {
        PrefsUtils.setTextSize(textSize)
        StyleUtils.reset()
        relaunch()
    }

    private fun setTheme(theme: Boolean) {
        PrefsUtils.setDarkTheme(theme)
        relaunch()
    }

    private fun setUseSystemKeyboard(useSystemKeyboard: Boolean) {
        PrefsUtils.setUseSystemKeyboard(useSystemKeyboard)
        relaunch()
    }

    private fun relaunch() {
        // Show progress as it takes a moment to relaunch
        mVgBusy!!.visibility = View.VISIBLE
        mVgContent!!.visibility = View.GONE
        // Relaunch as though launched from home screen
        val activity = activity
        if (activity != null) {
            val i = activity.packageManager
                    .getLaunchIntentForPackage(activity.packageName)
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                i.putExtra(CryptogramActivity.EXTRA_LAUNCH_SETTINGS, true)
                startActivity(i)
            } else {
                Crashlytics.logException(IllegalStateException("No launch intent available"))
            }
            activity.finish()
        } else {
            Crashlytics.logException(IllegalStateException("Fragment not attached"))
        }
    }

    private fun updateCompoundButton(compoundButton: CompoundButton, checked: Boolean,
                                     listener: CompoundButton.OnCheckedChangeListener) {
        compoundButton.setOnCheckedChangeListener(null)
        compoundButton.isChecked = checked
        compoundButton.setOnCheckedChangeListener(listener)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {

        }
        return super.onOptionsItemSelected(item)
    }

    @OnClick(R.id.bt_reset_dialogs)
    fun onClickResetDialogs() {
        PrefsUtils.setNeverAskRevealLetter(false)
        PrefsUtils.setNeverAskRevealMistakes(false)
        update()
    }

    @OnClick(R.id.bt_reset_all_puzzles)
    fun onClickResetAllPuzzles() {
        val keyword = getString(R.string.reset_all_puzzles_keyword)
        MaterialDialog.Builder(activity!!)
                .content(getString(R.string.reset_all_puzzles_confirmation, keyword))
                .positiveText(R.string.reset)
                .input(null, null) { _, input ->
                    val activity = activity as BaseActivity?
                    if (input.toString().trim { it <= ' ' }.toLowerCase(Locale.ENGLISH) == keyword) {
                        PuzzleProvider.getInstance(context).resetAll()
                        activity!!.showSnackbar(getString(R.string.reset_all_puzzles_executed))
                    } else {
                        activity!!.showSnackbar(getString(R.string.reset_all_puzzles_cancelled))
                    }
                }
                .show()
    }

}
