package com.pixplicity.cryptogram.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.CompoundButton
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.activities.BaseActivity
import com.pixplicity.cryptogram.activities.CryptogramActivity
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.PuzzleProvider
import com.pixplicity.cryptogram.utils.StyleUtils
import kotlinx.android.synthetic.main.fragment_settings.*
import java.util.*


class SettingsFragment : BaseFragment() {

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

        iv_theme_light.setOnClickListener {
            rb_theme_light.isChecked = true
        }
        iv_theme_dark.setOnClickListener {
            rb_theme_dark.isChecked = true
        }
        bt_reset_dialogs.setOnClickListener {
            PrefsUtils.setNeverAskRevealLetter(false)
            PrefsUtils.setNeverAskRevealMistakes(false)
            update()
        }
        bt_reset_all_puzzles.setOnClickListener {
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

        update()
    }

    private fun update() {
        // Theme settings
        updateCompoundButton(rb_theme_light, !PrefsUtils.getDarkTheme(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTheme(false)
            }
        })
        updateCompoundButton(rb_theme_dark, PrefsUtils.getDarkTheme(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTheme(true)
            }
        })

        // Display settings
        updateCompoundButton(rb_text_size_small, PrefsUtils.getTextSize() == -1, CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTextSize(-1)
            }
        })
        updateCompoundButton(rb_text_size_normal, PrefsUtils.getTextSize() == 0, CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTextSize(0)
            }
        })
        updateCompoundButton(rb_text_size_large, PrefsUtils.getTextSize() == 1, CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setTextSize(1)
            }
        })
        updateCompoundButton(cb_show_topic, PrefsUtils.getShowTopic(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setShowTopic(checked) })
        updateCompoundButton(cb_show_score, PrefsUtils.getShowScore(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setShowScore(checked) })

        // Keyboard settings
        updateCompoundButton(rb_keyboard_builtin, !PrefsUtils.getUseSystemKeyboard(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                setUseSystemKeyboard(false)
            }
        })
        updateCompoundButton(rb_keyboard_system, PrefsUtils.getUseSystemKeyboard(), CompoundButton.OnCheckedChangeListener { _, checked ->
            if (checked) {
                // Show warning that it works for shit
                MaterialDialog.Builder(activity!!)
                        .content(R.string.keyboard_system_dialog)
                        .positiveText(R.string.keyboard_system_dialog_ok)
                        .dismissListener { setUseSystemKeyboard(true) }
                        .show()
            }
        })
        updateCompoundButton(cb_show_hints, PrefsUtils.getShowUsedChars(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setShowUsedChars(checked) })
        updateCompoundButton(cb_auto_advance, PrefsUtils.getAutoAdvance(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setAutoAdvance(checked) })
        updateCompoundButton(cb_skip_filled_cells, PrefsUtils.getSkipFilledCells(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setSkipFilledCells(checked) })

        // Other settings
        updateCompoundButton(cb_randomize, PrefsUtils.getRandomize(),
                CompoundButton.OnCheckedChangeListener { _, checked -> PrefsUtils.setRandomize(checked) })

        bt_reset_dialogs.isEnabled = PrefsUtils.getNeverAskRevealLetter() || PrefsUtils.getNeverAskRevealMistakes()
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
        vg_busy.visibility = View.VISIBLE
        vg_content.visibility = View.GONE
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

}
