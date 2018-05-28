package com.pixplicity.cryptogram.activities

import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentSender
import android.graphics.PointF
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v4.view.GravityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.util.SparseBooleanArray
import android.view.*
import android.widget.Button
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import butterknife.BindView
import butterknife.OnClick
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ContentViewEvent
import com.crashlytics.android.answers.LoginEvent
import com.crashlytics.android.answers.ShareEvent
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.images.ImageManager
import com.google.android.gms.drive.Drive
import com.google.android.gms.games.Games
import com.google.android.gms.games.GamesActivityResultCodes
import com.google.android.gms.games.snapshot.SnapshotMetadata
import com.google.android.gms.games.snapshot.Snapshots
import com.google.firebase.analytics.FirebaseAnalytics
import com.pixplicity.cryptogram.BuildConfig
import com.pixplicity.cryptogram.CryptogramApp
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.adapters.PuzzleAdapter
import com.pixplicity.cryptogram.events.PuzzleEvent
import com.pixplicity.cryptogram.models.Puzzle
import com.pixplicity.cryptogram.utils.*
import com.pixplicity.cryptogram.views.CryptogramView
import com.pixplicity.cryptogram.views.HintView
import com.pixplicity.easyprefs.library.Prefs
import com.pixplicity.generate.Rate
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_cryptogram.*
import kotlinx.android.synthetic.main.in_drawer.*
import net.soulwolf.widget.ratiolayout.widget.RatioFrameLayout
import java.util.*
import java.util.concurrent.TimeUnit

class CryptogramActivity : BaseActivity(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private var mVwKeyboard: View? = null

    private var mAdapter: PuzzleAdapter? = null

    private var mRate: Rate? = null

    private var mGoogleApiClient: GoogleApiClient? = null

    // Are we currently resolving a connection failure?
    private var mResolvingConnectionFailure = false

    // Has the user clicked the sign-in button?
    private var mSignInClicked = false

    // Automatically start the sign-in flow when the Activity starts
    private var mAutoStartSignInFlow = false

    private var mLastConnectionError: Int = 0

    private var mFreshInstall: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isDarkTheme) {
            setTheme(R.style.AppTheme_Dark)
            window.setBackgroundDrawableResource(R.drawable.bg_activity_dark)
        }
        setContentView(R.layout.activity_cryptogram)
        if (isDarkTheme) {
            vg_stats.setBackgroundResource(R.drawable.bg_statistics_dark)
        }

        val puzzleProvider = PuzzleProvider.getInstance(this)

        // Create the Google Api Client with access to Games
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .addApi(Drive.API).addScope(Drive.SCOPE_APPFOLDER)
                .build()

        mRate = Rate.Builder(this)
                .setTriggerCount(10)
                .setMinimumInstallTime(TimeUnit.DAYS.toMillis(2).toInt())
                .setMessage(getString(R.string.rating, getString(R.string.app_name)))
                .setFeedbackAction(Uri.parse("mailto:paul+cryptogram@pixplicity.com"))
                .build()

        rv_drawer.layoutManager = LinearLayoutManager(this)
        mAdapter = PuzzleAdapter(this) { position ->
            if (mDrawerLayout != null) {
                mDrawerLayout!!.closeDrawers()
            }
            onPuzzleChanged(puzzleProvider.get(position), false)
        }
        rv_drawer.adapter = mAdapter

        vg_cryptogram.setCryptogramView(cryptogram)
        cryptogram.setOnHighlightListener(object : CryptogramView.OnHighlightListener {
            private val mHighlightShown = SparseBooleanArray()

            override fun onHighlight(type: Int, point: PointF) {
                if (mHighlightShown.get(type, false)) {
                    return
                }
                if (PrefsUtils.getHighlighted(type)) {
                    return
                }
                mHighlightShown.put(type, true)
                when (type) {
                    PrefsUtils.TYPE_HIGHLIGHT_HYPHENATION -> showHighlight(type, createTapTargetFromPoint(
                            point,
                            getString(R.string.highlight_hyphenation_title),
                            getString(R.string.highlight_hyphenation_description)),
                            1200
                    )
                    PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT -> if (mFreshInstall) {
                        PrefsUtils.setHighlighted(type, true)
                    } else {
                        showHighlight(PrefsUtils.TYPE_HIGHLIGHT_TOUCH_INPUT, createTapTargetFromPoint(
                                point,
                                getString(R.string.highlight_touch_input_title),
                                getString(R.string.highlight_touch_input_description)),
                                1200
                        )
                    }
                }
            }
        })

        if (PrefsUtils.getUseSystemKeyboard()) {
            vs_keyboard.visibility = View.GONE
        } else {
            mVwKeyboard = vs_keyboard.inflate()
            vs_keyboard.visibility = View.VISIBLE
            cryptogram.setKeyboardView(mVwKeyboard)
        }

        val intent = intent
        if (intent != null) {
            if (intent.getBooleanExtra(EXTRA_LAUNCH_SETTINGS, false)) {
                startActivity(SettingsActivity.create(this))
            }
        }

        if (UpdateManager.consumeEnabledShowUsedLetters()) {
            showHighlight(-1, TapTarget.forToolbarOverflow(
                    mToolbar,
                    getString(R.string.highlight_used_letters_title),
                    getString(R.string.highlight_used_letters_description)),
                    1200
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }

    override fun onStart() {
        super.onStart()

        EventProvider.bus.register(this)

        mGoogleApiClient!!.connect()

        val puzzleProvider = PuzzleProvider.getInstance(this)
        val puzzle = puzzleProvider.current
        puzzle?.onResume()
        showHintView(puzzle)

        if (hasOnBoardingPages()) {
            showOnboarding(0)
        } else {
            onGameplayReady()
        }
    }

    override fun onResume() {
        super.onResume()

        val puzzleProvider = PuzzleProvider.getInstance(this)
        val puzzle = puzzleProvider.current
        onPuzzleChanged(puzzle, true)
    }

    override fun onStop() {
        super.onStop()

        if (mGoogleApiClient!!.isConnected) {
            mGoogleApiClient!!.disconnect()
        }

        val puzzleProvider = PuzzleProvider.getInstance(this)
        val puzzle = puzzleProvider.current
        puzzle?.onPause()

        EventProvider.bus.unregister(this)
    }

    override fun onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout!!.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout!!.closeDrawer(GravityCompat.START)
            return
        }
        if (mVwKeyboard != null && mVwKeyboard!!.isShown) {
            cryptogram.hideSoftInput()
            return
        }
        super.onBackPressed()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        Log.d(TAG, "onActivityResult: $requestCode")
        super.onActivityResult(requestCode, resultCode, intent)
        when (requestCode) {
            RC_PLAY_GAMES -> {
                run {
                    Log.d(TAG, "onActivityResult: resolution result")
                    mSignInClicked = false
                    mResolvingConnectionFailure = false
                    when (resultCode) {
                        RESULT_OK -> {
                            // Logged in
                            run {
                                // Analytics
                                CryptogramApp.getInstance().firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, null)
                                Answers.getInstance().logLogin(LoginEvent().putSuccess(true))
                            }
                            mGoogleApiClient!!.connect()
                        }
                        GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED -> {
                            // Logged out
                            if (mGoogleApiClient!!.isConnected) {
                                mGoogleApiClient!!.disconnect()
                            }
                        }
                        RESULT_CANCELED -> {
                            // Canceled; do nothing
                        }
                        else -> {
                            // Assume some error
                            showGmsError(resultCode)
                        }
                    }
                }
                if (mDrawerLayout != null) {
                    mDrawerLayout!!.closeDrawers()
                }
                if (intent != null) {
                    if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                        // Load a snapshot.
                        val pd = ProgressDialog(this)
                        pd.setMessage("Loading saved game...")
                        pd.show()
                        val snapshotMetadata = intent.getParcelableExtra<SnapshotMetadata>(Snapshots.EXTRA_SNAPSHOT_METADATA)
                        PuzzleProvider.getInstance(this).load(mGoogleApiClient, snapshotMetadata,
                                object : SavegameManager.OnLoadResult {
                                    override fun onLoadSuccess() {
                                        onPuzzleChanged(PuzzleProvider.getInstance(this@CryptogramActivity)
                                                .current, false)
                                        showSnackbar("Game loaded.")
                                        pd.dismiss()
                                    }

                                    override fun onLoadFailure() {
                                        showSnackbar("Sorry, the game state couldn't be restored.")
                                        pd.dismiss()
                                    }
                                })
                    } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
                        PuzzleProvider.getInstance(this).save(mGoogleApiClient,
                                object : SavegameManager.OnSaveResult {
                                    override fun onSaveSuccess() {
                                        showSnackbar("Game saved.")
                                    }

                                    override fun onSaveFailure() {
                                        showSnackbar("Game couldn't be saved at this time.")
                                    }
                                })
                    }
                }
            }
            RC_SAVED_GAMES -> {
                if (mDrawerLayout != null) {
                    mDrawerLayout!!.closeDrawers()
                }
                if (intent != null) {
                    if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_METADATA)) {
                        val pd = ProgressDialog(this)
                        pd.setMessage("Loading saved game...")
                        pd.show()
                        val snapshotMetadata = intent.getParcelableExtra<SnapshotMetadata>(Snapshots.EXTRA_SNAPSHOT_METADATA)
                        PuzzleProvider.getInstance(this).load(mGoogleApiClient, snapshotMetadata, object : SavegameManager.OnLoadResult {
                            override fun onLoadSuccess() {
                                onPuzzleChanged(PuzzleProvider.getInstance(this@CryptogramActivity).current, false)
                                showSnackbar("Game loaded.")
                                pd.dismiss()
                            }

                            override fun onLoadFailure() {
                                showSnackbar("Sorry, the game state couldn't be restored.")
                                pd.dismiss()
                            }
                        })
                    } else if (intent.hasExtra(Snapshots.EXTRA_SNAPSHOT_NEW)) {
                        PuzzleProvider.getInstance(this).save(mGoogleApiClient, object : SavegameManager.OnSaveResult {
                            override fun onSaveSuccess() {
                                showSnackbar("Game saved.")
                            }

                            override fun onSaveFailure() {
                                showSnackbar("Game couldn't be saved at this time.")
                            }
                        })
                    }
                }
            }
        }
    }

    private fun hasOnBoardingPages(): Boolean {
        return PrefsUtils.getOnboarding() < ONBOARDING_PAGES - 1
    }

    private fun createTapTargetFromPoint(point: PointF, title: String,
                                         description: String): TapTarget {
        val viewRect = Rect()
        cryptogram.getGlobalVisibleRect(viewRect)
        val targetX = (point.x + viewRect.left).toInt()
        val targetY = (point.y + viewRect.top).toInt()
        val targetRadius = 48
        return TapTarget.forBounds(Rect(targetX - targetRadius, targetY - targetRadius, targetX + targetRadius, targetY + targetRadius),
                title, description)
    }

    private fun showHighlight(type: Int, tapTarget: TapTarget, delayMillis: Int) {
        Handler().postDelayed({
            val showTime = System.currentTimeMillis()
            TapTargetView.showFor(
                    this@CryptogramActivity,
                    tapTarget
                            .titleTextColor(R.color.white)
                            .descriptionTextColor(R.color.white)
                            .outerCircleColor(R.color.highlight_color)
                            .cancelable(true)
                            .tintTarget(false)
                            .transparentTarget(true),
                    object : TapTargetView.Listener() {
                        override fun onTargetClick(view: TapTargetView) {
                            dismiss(view)
                        }

                        override fun onOuterCircleClick(view: TapTargetView?) {
                            dismiss(view)
                        }

                        override fun onTargetCancel(view: TapTargetView) {
                            dismiss(view)
                        }

                        private fun dismiss(view: TapTargetView?) {
                            if (type >= 0 && System.currentTimeMillis() - showTime >= 1500) {
                                // Ensure that the user saw the message
                                PrefsUtils.setHighlighted(type, true)
                            }
                            view!!.dismiss(false)
                        }
                    })
        }, delayMillis.toLong())
    }

    private fun showOnboarding(page: Int) {
        val titleStringResId: Int
        val textStringResId: Int
        var actionStringResId = R.string.intro_next
        val video: VideoUtils.Video
        when (page) {
            0 -> {
                titleStringResId = R.string.intro1_title
                textStringResId = R.string.intro1_text
                video = VideoUtils.VIDEO_INSTRUCTION
            }
            1 -> {
                titleStringResId = R.string.intro2_title
                textStringResId = R.string.intro2_text
                actionStringResId = R.string.intro_done
                video = VideoUtils.VIDEO_HELP
            }
            ONBOARDING_PAGES -> {
                onGameplayReady()
                return
            }
            else -> {
                onGameplayReady()
                return
            }
        }

        val onboarding = PrefsUtils.getOnboarding()
        if (onboarding == -1) {
            mFreshInstall = true
        }
        if (onboarding >= page) {
            showOnboarding(page + 1)
            return
        }

        val customView = LayoutInflater.from(this).inflate(R.layout.dialog_intro, null)

        val tvIntro = customView.findViewById<TextView>(R.id.tv_intro)
        tvIntro.setText(textStringResId)

        val vgRatio = customView.findViewById<RatioFrameLayout>(R.id.vg_ratio)
        val player = VideoUtils.setup(this, vgRatio, video)

        MaterialDialog.Builder(this)
                .title(titleStringResId)
                .customView(customView, false)
                .cancelable(false)
                .positiveText(actionStringResId)
                .showListener { dialogInterface ->
                    player?.start()
                }
                .onAny { dialog, which ->
                    PrefsUtils.setOnboarding(page)
                    showOnboarding(page + 1)
                }
                .show()
    }

    @OnClick(R.id.vg_google_play_games)
    fun onClickGooglePlayGames() {
        if (mGoogleApiClient!!.isConnected) {
            // Connected; show gameplay options
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_google_play_games, null)
            val dialog = AlertDialog.Builder(this)
                    .setView(dialogView)
                    .show()

            val btLeaderboards = dialogView.findViewById<Button>(R.id.bt_leaderboards)
            btLeaderboards.setOnClickListener { view ->
                dialog.dismiss()
                run {
                    // Analytics
                    CryptogramApp.getInstance().firebaseAnalytics.logEvent(CryptogramApp.CONTENT_LEADERBOARDS, null)
                    Answers.getInstance().logContentView(ContentViewEvent().putContentName(CryptogramApp.CONTENT_LEADERBOARDS))
                }
                try {
                    startActivityForResult(
                            Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, getString(R.string.leaderboard_scoreboard)),
                            RC_UNUSED)
                } catch (e: SecurityException) {
                    // Not sure why we're still seeing errors about the connection state, but here we are
                    Crashlytics.logException(e)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@CryptogramActivity, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show()
                }
            }

            val btAchievements = dialogView.findViewById<Button>(R.id.bt_achievements)
            btAchievements.setOnClickListener { view ->
                dialog.dismiss()
                run {
                    // Analytics
                    CryptogramApp.getInstance().firebaseAnalytics.logEvent(CryptogramApp.CONTENT_ACHIEVEMENTS, null)
                    Answers.getInstance().logContentView(ContentViewEvent().putContentName(CryptogramApp.CONTENT_ACHIEVEMENTS))
                }
                try {
                    startActivityForResult(
                            Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                            RC_UNUSED)
                } catch (e: SecurityException) {
                    // Not sure why we're still seeing errors about the connection state, but here we are
                    Crashlytics.logException(e)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this@CryptogramActivity, R.string.google_play_games_not_installed, Toast.LENGTH_LONG).show()
                }
            }

            val btRestoreSavedGames = dialogView.findViewById<Button>(R.id.bt_restore_saved_games)
            btRestoreSavedGames.setOnClickListener { view ->
                dialog.dismiss()
                val maxNumberOfSavedGamesToShow = 5
                val savedGamesIntent = Games.Snapshots.getSelectSnapshotIntent(mGoogleApiClient,
                        "See My Saves", true, true, maxNumberOfSavedGamesToShow)
                startActivityForResult(savedGamesIntent, RC_SAVED_GAMES)
            }

            val btSignOut = dialogView.findViewById<Button>(R.id.bt_sign_out)
            btSignOut.setOnClickListener { view ->
                dialog.dismiss()
                mSignInClicked = false
                Games.signOut(mGoogleApiClient!!)
                if (mGoogleApiClient!!.isConnected) {
                    mGoogleApiClient!!.disconnect()
                }
                updateGooglePlayGames()
            }
        } else {
            // start the sign-in flow
            mSignInClicked = true
            mGoogleApiClient!!.connect()
        }
    }

    private fun updateCryptogram(puzzle: Puzzle?) {
        if (puzzle != null) {
            val provider = PuzzleProvider.getInstance(this)
            provider.setCurrentId(puzzle.id)
            rv_drawer.scrollToPosition(provider.currentIndex)
            tv_error.visibility = View.GONE
            vg_cryptogram.visibility = View.VISIBLE
            // Apply the puzzle to the CryptogramView
            cryptogram.puzzle = puzzle
            // Show other puzzle details
            val author = puzzle.author
            if (author == null) {
                tv_author.visibility = View.GONE
            } else {
                tv_author.visibility = View.VISIBLE
                tv_author.text = getString(R.string.quote, author)
            }
            val topic = puzzle.topic
            if (!PrefsUtils.getShowTopic() && !puzzle.isCompleted || topic == null) {
                tv_topic.visibility = View.GONE
            } else {
                tv_topic.visibility = View.VISIBLE
                tv_topic.text = getString(R.string.topic, topic)
            }
            if (puzzle.isInstruction || puzzle.isNoScore) {
                setToolbarSubtitle(puzzle.getTitle(this))
            } else {
                setToolbarSubtitle(getString(
                        R.string.puzzle_number,
                        puzzle.number))
            }
            // Invoke various events
            showPuzzleState(puzzle)
            puzzle.onResume()
        } else {
            tv_error.visibility = View.VISIBLE
            vg_cryptogram.visibility = View.GONE
            setToolbarSubtitle(null)
        }
    }

    private fun onGameplayReady() {
        cryptogram.requestFocus()
        if (UpdateManager.consumeScoreExcludesExcessInputs()) {
            MaterialDialog.Builder(this)
                    .title(R.string.scoring_changed_title)
                    .content(R.string.scoring_changed_message)
                    .cancelable(false)
                    .positiveText(R.string.scoring_changed_ok)
                    .show()
        }
    }

    fun showPuzzleState(puzzle: Puzzle?) {
        // Update the HintView as the puzzle updates
        mAdapter!!.notifyDataSetChanged()
        if (puzzle!!.isCompleted) {
            vg_stats.visibility = View.VISIBLE
            val durationMs = puzzle.durationMs
            if (durationMs <= 0) {
                vg_stats_time.visibility = View.GONE
            } else {
                vg_stats_time.visibility = View.VISIBLE
                tv_stats_time.text = StringUtils.getDurationString(durationMs)
            }
            var reveals = -1
            var score: Float? = null
            if (PrefsUtils.getShowScore()) {
                reveals = puzzle.reveals
                score = puzzle.score
            }
            if (reveals < 0) {
                vg_stats_reveals.visibility = View.GONE
            } else {
                vg_stats_reveals.visibility = View.VISIBLE
                tv_stats_reveals.text = reveals.toString()
            }
            if (score != null) {
                vg_stats_practice.visibility = View.GONE
                vg_stats_score.visibility = View.VISIBLE
                tv_stats_score.text = String.format(
                        Locale.ENGLISH,
                        "%.1f%%",
                        score * 100)
            } else {
                vg_stats_score.visibility = View.GONE
                vg_stats_practice.visibility = if (puzzle.isNoScore) View.VISIBLE else View.GONE
            }
        } else {
            vg_stats.visibility = View.GONE
        }
        showHintView(puzzle)
    }

    protected fun showHintView(puzzle: Puzzle?) {
        hint.visibility = if (puzzle != null && !puzzle.isCompleted
                && PrefsUtils.getShowUsedChars() && PrefsUtils.getUseSystemKeyboard())
            View.VISIBLE
        else
            View.GONE
    }

    fun onPuzzleChanged(puzzle: Puzzle?, delayEvent: Boolean) {
        updateCryptogram(puzzle)
        if (delayEvent) {
            EventProvider.postEventDelayed(PuzzleEvent.PuzzleProgressEvent(puzzle), 200)
        } else {
            EventProvider.postEvent(PuzzleEvent.PuzzleProgressEvent(puzzle))
        }
    }

    @Subscribe
    fun onPuzzleProgress(event: PuzzleEvent.PuzzleProgressEvent) {
        showPuzzleState(event.puzzle)
    }

    @Subscribe
    fun onPuzzleStarted(event: PuzzleEvent.PuzzleStartedEvent) {
        mGoogleApiClient?.let {
            if (it.isConnected) {
                // Submit any achievements
                AchievementProvider.onCryptogramStart(it)
            }
        }
    }

    @Subscribe
    fun onPuzzleReset(event: PuzzleEvent.PuzzleResetEvent) {
        updateCryptogram(event.puzzle)
    }

    @Subscribe
    fun onPuzzleCompleted(event: PuzzleEvent.PuzzleCompletedEvent) {
        updateCryptogram(event.puzzle)

        // Increment the trigger for displaying the rating dialog
        mRate!!.count()

        // Allow the rating dialog to appear if needed
        mRate!!.showRequest()

        // Conditional behavior after X triggers
        // FIXME if only we could use mRate.getCount() here
        val count = Prefs.getLong("launch_count_l", 0L)
        if (count == 100L || count == 300L) {
            // Prompt for donations
            // TODO only display if user hasn't donated
            // TODO display dialog
        }

        mGoogleApiClient?.let {
            if (it.isConnected) {
                // Submit score
                LeaderboardProvider.submit(it)

                // Submit any achievements
                AchievementProvider.onCryptogramCompleted(it)
            }

            // Attempt to save the game to Google Play Saved Games
            PuzzleProvider.getInstance(this).save(it, null)
        }
    }

    @Subscribe
    fun onPuzzleKeyboardInput(event: PuzzleEvent.KeyboardInputEvent) {
        val keyCode = event.keyCode
        cryptogram.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                keyCode, 0))
        cryptogram.dispatchKeyEvent(KeyEvent(0, 0, KeyEvent.ACTION_UP,
                keyCode, 0))
    }

    override fun onDrawerOpened(drawerView: View) {}

    override fun onDrawerClosed(drawerView: View) {}

    override fun onDrawerMoving() {
        cryptogram.hideSoftInput()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_cryptogram, menu)
        run {
            val item = menu.findItem(R.id.action_reveal_puzzle)
            item.setVisible(BuildConfig.DEBUG)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (super.onOptionsItemSelected(item)) {
            return true
        }

        val puzzle = cryptogram.puzzle
        when (item.itemId) {
            R.id.action_next -> {
                run { nextPuzzle() }
                return true
            }
            R.id.action_reveal_letter -> {
                run {
                    if (puzzle == null || !cryptogram.hasSelectedCharacter()) {
                        showSnackbar(getString(R.string.reveal_letter_instruction))
                    } else {
                        if (PrefsUtils.getNeverAskRevealLetter()) {
                            cryptogram.revealCharacterMapping(
                                    cryptogram.selectedCharacter)
                        } else {
                            MaterialDialog.Builder(this)
                                    .content(R.string.reveal_letter_confirmation)
                                    .checkBoxPromptRes(R.string.never_ask_again, false, null)
                                    .positiveText(R.string.reveal)
                                    .onPositive { dialog, which ->
                                        PrefsUtils.setNeverAskRevealLetter(dialog.isPromptCheckBoxChecked)
                                        cryptogram.revealCharacterMapping(
                                                cryptogram.selectedCharacter)
                                    }
                                    .negativeText(R.string.cancel)
                                    .show()
                        }
                    }
                }
                return true
            }
            R.id.action_reveal_mistakes -> {
                run {
                    if (PrefsUtils.getNeverAskRevealMistakes()) {
                        cryptogram.revealMistakes()
                    } else {
                        MaterialDialog.Builder(this)
                                .content(R.string.reveal_mistakes_confirmation)
                                .checkBoxPromptRes(R.string.never_ask_again, false, null)
                                .positiveText(R.string.reveal)
                                .onPositive { dialog, which ->
                                    PrefsUtils.setNeverAskRevealMistakes(dialog.isPromptCheckBoxChecked)
                                    cryptogram.revealMistakes()
                                }
                                .negativeText(R.string.cancel)
                                .show()
                    }
                }
                return true
            }
            R.id.action_reveal_puzzle -> {
                run {
                    if (BuildConfig.DEBUG) {
                        puzzle?.revealPuzzle()
                        cryptogram.redraw()
                    } else {
                        throw IllegalStateException("Only applicable to debug builds")
                    }
                }
                return true
            }
            R.id.action_reset -> {
                run {
                    if (puzzle != null) {
                        AlertDialog.Builder(this)
                                .setMessage(R.string.reset_puzzle)
                                .setPositiveButton(R.string.reset) { dialogInterface, i ->
                                    puzzle.reset(true)
                                    cryptogram.reset()
                                    showPuzzleState(puzzle)
                                    onPuzzleChanged(puzzle, false)
                                }
                                .setNegativeButton(R.string.cancel) { dialogInterface, i -> }
                                .show()
                    }
                }
                return true
            }
            R.id.action_go_to -> {
                if (puzzle == null) {
                    if (mDrawerLayout != null) {
                        mDrawerLayout!!.openDrawer(GravityCompat.START)
                    }
                } else {
                    var prefilledText: String? = null
                    val currentId = puzzle.number
                    if (currentId > 0) {
                        prefilledText = currentId.toString()
                    }
                    MaterialDialog.Builder(this)
                            .content(R.string.go_to_puzzle_content)
                            .inputType(InputType.TYPE_CLASS_NUMBER)
                            .input(null, prefilledText) { dialog, input ->
                                val button = dialog.getActionButton(DialogAction.POSITIVE)
                                try {
                                    button.isEnabled = Integer.parseInt(input.toString()) > 0
                                } catch (ignored: NumberFormatException) {
                                    button.isEnabled = false
                                }
                            }
                            .alwaysCallInputCallback()
                            .showListener { dialogInterface ->
                                val dialog = dialogInterface as MaterialDialog

                                dialog.inputEditText!!.selectAll()
                            }
                            .onPositive { dialog, which ->

                                val input = dialog.inputEditText!!.text
                                try {
                                    val puzzleNumber = Integer.parseInt(input.toString())
                                    val provider = PuzzleProvider
                                            .getInstance(this@CryptogramActivity)
                                    val newPuzzle = provider.getByNumber(puzzleNumber)
                                    if (newPuzzle == null) {
                                        showSnackbar(getString(R.string.puzzle_nonexistant, puzzleNumber))
                                    } else {
                                        onPuzzleChanged(newPuzzle, false)
                                    }
                                } catch (ignored: NumberFormatException) {
                                }
                            }.show()
                }
                return true
            }
            R.id.action_share -> {
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                val text: String
                if (puzzle != null && puzzle.isCompleted) {
                    text = getString(
                            R.string.share_full,
                            puzzle.text,
                            puzzle.author,
                            getString(R.string.share_url))
                } else {
                    text = getString(
                            R.string.share_partial,
                            if (puzzle == null)
                                getString(R.string.author_unknown)
                            else
                                puzzle.author,
                            getString(R.string.share_url))
                }
                intent.putExtra(Intent.EXTRA_TEXT, text)
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
                run {
                    // Analytics
                    val puzzleId = puzzle?.id?.toString()
                    val bundle = Bundle()
                    bundle.putString(FirebaseAnalytics.Param.LEVEL, puzzleId)
                    bundle.putString(FirebaseAnalytics.Param.CONTENT, text)
                    CryptogramApp.getInstance().firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle)
                    Answers.getInstance().logShare(
                            ShareEvent()
                                    .putContentId(puzzleId)
                                    .putContentType("puzzle")
                                    .putContentName(text)
                    )
                }
                return true
            }
            R.id.action_stats -> {
                run {
                    // Analytics
                    CryptogramApp.getInstance().firebaseAnalytics.logEvent(CryptogramApp.CONTENT_STATISTICS, null)
                    Answers.getInstance().logContentView(ContentViewEvent().putContentName(CryptogramApp.CONTENT_STATISTICS))
                }
                // Compose the dialog
                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_statistics, null) as TableLayout
                puzzle?.save()
                val provider = PuzzleProvider.getInstance(this)
                var count = 0
                var scoreCount = 0
                var score = 0f
                var shortestDurationMs: Long = 0
                var totalDurationMs: Long = 0
                for (c in provider.all) {
                    val durationMs = c.progress.durationMs
                    if (!c.isInstruction && c.isCompleted) {
                        count++
                        val puzzleScore = c.score ?: continue
                        score += puzzleScore
                        scoreCount++
                        if (shortestDurationMs == 0L || shortestDurationMs > durationMs) {
                            shortestDurationMs = durationMs
                        }
                    }
                    totalDurationMs += durationMs
                }
                val scoreAverageText: String
                if (scoreCount > 0) {
                    scoreAverageText = getString(R.string.stats_average_score_format, score / scoreCount.toFloat() * 100f)
                } else {
                    scoreAverageText = getString(R.string.not_applicable)
                }
                val scoreCumulativeText = getString(R.string.stats_cumulative_score_format, score * 100f)
                val fastestCompletion: String
                if (shortestDurationMs == 0L) {
                    fastestCompletion = getString(R.string.not_applicable)
                } else {
                    fastestCompletion = StringUtils.getDurationString(shortestDurationMs)
                }
                AchievementProvider.AchievementStats.calculate(this)
                val longestStreak = AchievementProvider.AchievementStats.longestStreak
                run {
                    val view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null)
                    (view.findViewById<View>(R.id.tv_label) as TextView).setText(R.string.stats_total_completed_label)
                    (view.findViewById<View>(R.id.tv_value) as TextView).text = getString(R.string.stats_total_completed_value,
                            count,
                            provider.lastNumber)
                    dialogView.addView(view)
                }
                run {
                    val view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null)
                    (view.findViewById<View>(R.id.tv_label) as TextView).setText(R.string.stats_average_score_label)
                    (view.findViewById<View>(R.id.tv_value) as TextView).text = getString(R.string.stats_average_score_value,
                            scoreAverageText)
                    dialogView.addView(view)
                }
                run {
                    val view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null)
                    (view.findViewById<View>(R.id.tv_label) as TextView).setText(R.string.stats_cumulative_score_label)
                    (view.findViewById<View>(R.id.tv_value) as TextView).text = getString(R.string.stats_cumulative_score_value,
                            scoreCumulativeText)
                    dialogView.addView(view)
                }
                run {
                    val view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null)
                    (view.findViewById<View>(R.id.tv_label) as TextView).setText(R.string.stats_fastest_completion_label)
                    (view.findViewById<View>(R.id.tv_value) as TextView).text = getString(R.string.stats_fastest_completion_value,
                            fastestCompletion)
                    dialogView.addView(view)
                }
                run {
                    val view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null)
                    (view.findViewById<View>(R.id.tv_label) as TextView).setText(R.string.stats_total_time_spent_label)
                    (view.findViewById<View>(R.id.tv_value) as TextView).text = getString(R.string.stats_total_time_spent_value,
                            StringUtils.getDurationString(totalDurationMs))
                    dialogView.addView(view)
                }
                run {
                    val view = LayoutInflater.from(this).inflate(R.layout.in_statistics_row, null)
                    (view.findViewById<View>(R.id.tv_label) as TextView).setText(R.string.stats_longest_streak_label)
                    (view.findViewById<View>(R.id.tv_value) as TextView).text = getString(R.string.stats_longest_streak_value,
                            longestStreak,
                            resources.getQuantityString(R.plurals.days, longestStreak))
                    dialogView.addView(view)
                }
                AlertDialog.Builder(this)
                        .setTitle(R.string.statistics)
                        .setView(dialogView)
                        .setPositiveButton(android.R.string.ok) { dialogInterface, i -> }
                        .show()
                return true
            }
            R.id.action_settings -> {
                startActivity(SettingsActivity.create(this))
                return true
            }
            R.id.action_how_to_play -> {
                startActivity(HowToPlayActivity.create(this))
                return true
            }
            R.id.action_about -> {
                startActivity(AboutActivity.create(this))
                return true
            }
            R.id.action_donate -> {
                startActivity(DonateActivity.create(this))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun nextPuzzle() {
        val puzzle = PuzzleProvider.getInstance(this).next
        onPuzzleChanged(puzzle, false)
    }

    // Google Play Services
    override fun onConnected(bundle: Bundle?) {
        mLastConnectionError = 0
        Log.d(TAG, "onConnected(): connected to Google APIs")

        updateGooglePlayGames()

        mGoogleApiClient?.let {
            if (it.isConnected) {
                // Submit score
                LeaderboardProvider.submit(it)

                // Submit any achievements
                AchievementProvider.check(it)
            }
        }
    }

    // Google Play Services
    override fun onConnectionSuspended(i: Int) {
        Log.d(TAG, "onConnectionSuspended(): attempting to connect")
        mGoogleApiClient!!.connect()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed: attempting to resolve")
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed: already resolving")
            return
        }

        mLastConnectionError = connectionResult.errorCode
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false
            mSignInClicked = false
            mResolvingConnectionFailure = true
            var noResolution = true
            if (connectionResult.hasResolution()) {
                try {
                    Log.d(TAG, "onConnectionFailed: offering resolution")
                    connectionResult.startResolutionForResult(this, RC_PLAY_GAMES)
                    noResolution = false
                } catch (e: IntentSender.SendIntentException) {
                    Crashlytics.logException(e)
                    Log.e(TAG, "onConnectionFailed: couldn't resolve", e)
                }

            }
            if (noResolution) {
                Log.e(TAG, "onConnectionFailed: no resolution for: " + connectionResult.toString())
                mResolvingConnectionFailure = false
                showGmsError(0)
            }
        }
        updateGooglePlayGames()
    }

    private fun updateGooglePlayGames() {
        if (mGoogleApiClient!!.isConnected) {
            // Set the greeting appropriately on main menu
            val p = Games.Players.getCurrentPlayer(mGoogleApiClient)
            val displayName: String
            val imageUri: Uri?
            if (p == null) {
                displayName = getString(R.string.google_play_games_player_unknown)
                imageUri = null
            } else {
                displayName = p.displayName
                imageUri = if (p.hasHiResImage()) p.hiResImageUri else p.iconImageUri
                //bannerUri = p.getBannerImageLandscapeUri();
            }
            Log.w(TAG, "onConnected(): current player is $displayName")

            iv_google_play_games_icon.visibility = View.GONE
            iv_google_play_games_avatar.visibility = View.VISIBLE
            ImageManager.create(this).loadImage(iv_google_play_games_avatar, imageUri, R.drawable.im_avatar)
            tv_google_play_games.visibility = View.GONE
            tv_google_play_games_name.visibility = View.VISIBLE
            tv_google_play_games_name.text = displayName
            vg_google_play_games_actions.visibility = View.VISIBLE
        } else {
            iv_google_play_games_icon.visibility = View.VISIBLE
            iv_google_play_games_avatar.visibility = View.GONE
            tv_google_play_games.visibility = View.VISIBLE
            tv_google_play_games_name.visibility = View.GONE
            vg_google_play_games_actions.visibility = View.GONE
        }
    }

    private fun showGmsError(errorCode: Int) {
        if (isFinishing) {
            return
        }
        AlertDialog.Builder(this)
                .setMessage(getString(R.string.google_play_games_connection_failure, mLastConnectionError, errorCode))
                .setPositiveButton(android.R.string.ok) { dialog, i -> dialog.dismiss() }
                .show()
    }

    companion object {

        private val TAG = CryptogramActivity::class.java.simpleName

        private val RC_UNUSED = 1000
        private val RC_PLAY_GAMES = 1001
        private val RC_SAVED_GAMES = 1002

        private val ONBOARDING_PAGES = 2

        val EXTRA_LAUNCH_SETTINGS = "launch_settings"
    }

}
