package com.pixplicity.cryptogram.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.design.widget.Snackbar
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.TextView

import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.StyleUtils

abstract class BaseActivity : AppCompatActivity() {

    var mVgRoot: View? = null

    var mDrawerLayout: DrawerLayout? = null

    var mVgCoordinator: View? = null

    var mToolbar: Toolbar? = null

    var mTvToolbarSubtitle: TextView? = null

    protected var mDrawerToggle: ActionBarDrawerToggle? = null

    var isDarkTheme: Boolean = false
        protected set

    protected val viewRoot: View?
        get() = if (mVgRoot == null) mDrawerLayout else mVgRoot

    override fun setContentView(@LayoutRes layoutResID: Int) {
        // Replace any splash screen image
        window.setBackgroundDrawableResource(R.drawable.bg_activity_light)
        isDarkTheme = PrefsUtils.darkTheme
        if (isDarkTheme) {
            setTheme(R.style.AppTheme_Dark)
            // Replace any splash screen image
            window.setBackgroundDrawableResource(R.drawable.bg_activity_dark)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
            window.setBackgroundDrawableResource(R.drawable.bg_activity_light)
        }
        super.setContentView(layoutResID)

        // FIXME find a way to use kotlin android extensions here
        mVgRoot = findViewById(R.id.vg_root)
        mDrawerLayout = findViewById(R.id.drawer_layout)
        mVgCoordinator = findViewById(R.id.coordinator)
        mToolbar = findViewById(R.id.toolbar)
        mTvToolbarSubtitle = findViewById(R.id.tv_toolbar_subtitle)

        setSupportActionBar(mToolbar)
        mToolbar?.contentInsetStartWithNavigation = 0

        val ab = supportActionBar
        ab.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        ab.elevation = 6f
        ab.setTitle(R.string.app_name)
        ab.setDisplayShowTitleEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isDarkTheme) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorDarkPrimaryDark)
            } else {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
            }
        }

        mDrawerLayout?.let {
            // Apply side navigation
            mDrawerToggle = object : ActionBarDrawerToggle(
                    this, /* host Activity */
                    it, /* DrawerLayout object */
                    R.string.drawer_open, /* "open drawer" description */
                    R.string.drawer_close  /* "close drawer" description */
            ) {
                /** Called when a drawer has settled in a completely open state.  */
                override fun onDrawerOpened(drawerView: View) {
                    super.onDrawerOpened(drawerView)
                    this@BaseActivity.onDrawerOpened(drawerView)
                }

                /** Called when a drawer has settled in a completely closed state.  */
                override fun onDrawerClosed(drawerView: View) {
                    super.onDrawerClosed(drawerView)
                    this@BaseActivity.onDrawerClosed(drawerView)
                }
            }

            // Set the drawer toggle as the DrawerListener
            it.addDrawerListener(mDrawerToggle!!)
            it.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

                override fun onDrawerOpened(drawerView: View) {}

                override fun onDrawerClosed(drawerView: View) {}

                override fun onDrawerStateChanged(newState: Int) {
                    when (newState) {
                        DrawerLayout.STATE_IDLE -> {
                        }
                        DrawerLayout.STATE_SETTLING, DrawerLayout.STATE_DRAGGING -> this@BaseActivity.onDrawerMoving()
                    }
                }
            })

            supportActionBar.setDisplayHomeAsUpEnabled(true)
            supportActionBar.setHomeButtonEnabled(true)
        }
    }

    protected fun setToolbarSubtitle(subtitle: String?) {
        if (mTvToolbarSubtitle != null) {
            mTvToolbarSubtitle!!.text = subtitle
        } else {
            mToolbar?.subtitle = subtitle
        }
    }

    protected open fun onDrawerOpened(drawerView: View?) {}

    protected open fun onDrawerClosed(drawerView: View?) {}

    protected open fun onDrawerMoving() {}

    override fun getSupportActionBar(): ActionBar {
        return super.getSupportActionBar()!!
    }

    protected fun setHomeButtonEnabled(enabled: Boolean) {
        val ab = supportActionBar
        ab.setHomeButtonEnabled(enabled)
        ab.setDisplayShowHomeEnabled(enabled)
        ab.setDisplayHomeAsUpEnabled(enabled)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        if (mDrawerToggle != null) {
            // Sync the toggle state after onRestoreInstanceState has occurred.
            mDrawerToggle!!.syncState()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (mDrawerToggle != null) {
            mDrawerToggle!!.onConfigurationChanged(newConfig)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the event to ActionBarDrawerToggle if it's present
        if (mDrawerToggle != null && mDrawerToggle!!.onOptionsItemSelected(item)) {
            // If it returns true, then it has handled the option item selection event
            return true
        }
        when (item.itemId) {
            android.R.id.home -> {
                run {
                    val up = Intent(this@BaseActivity, CryptogramActivity::class.java)
                    if (NavUtils.shouldUpRecreateTask(this@BaseActivity, up)) {
                        TaskStackBuilder.create(this@BaseActivity)
                                .addNextIntent(up)
                                .startActivities()
                        finish()
                    } else {
                        NavUtils.navigateUpTo(this@BaseActivity, up)
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showSnackbar(text: String) {
        val snackbar = Snackbar.make(viewRoot!!, text, Snackbar.LENGTH_SHORT)
        val snackBarView = snackbar.view

        // Set background
        @ColorInt val colorPrimary = StyleUtils.getColor(this, R.attr.colorPrimary)
        snackBarView.setBackgroundColor(colorPrimary)

        // Set foreground
        @ColorInt val textColor = StyleUtils.getColor(this, R.attr.textColorOnPrimary)
        val textView = snackBarView.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.setTextColor(textColor)

        snackbar.show()
    }

}
