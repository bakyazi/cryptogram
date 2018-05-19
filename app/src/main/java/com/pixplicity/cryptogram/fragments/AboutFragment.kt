package com.pixplicity.cryptogram.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.*
import android.widget.Toast
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.HtmlCompat
import com.pixplicity.cryptogram.views.SimpleInputConnection
import kotlinx.android.synthetic.main.fragment_about.*


class AboutFragment : BaseFragment() {

    companion object {
        private val TAG = AboutFragment::class.java.simpleName
        val FEEDBACK_EMAIL = "paul@pixplicity.com"
    }

    private val versionString: String?
        get() {
            var versionString: String? = null
            try {
                val info = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0)
                versionString = getString(R.string.version, info.versionName, info.versionCode)
            } catch (e: PackageManager.NameNotFoundException) {
                Log.e(TAG, "Package not found", e)
            }

            return versionString
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_about, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDarkTheme) {
            invert(iv_logo)
        }
        // App version
        val versionString = versionString
        tv_version.text = versionString

        // About this app
        val appName = getString(R.string.app_name)
        tv_about_this_app_1.text = HtmlCompat.fromHtml(getString(R.string.about_this_app_1, appName))
        tv_about_this_app_2.text = HtmlCompat.fromHtml(getString(R.string.about_this_app_2))

        // Legal
        tv_disclaimer.text = HtmlCompat.fromHtml(getString(R.string.disclaimer))

        // Licenses
        tv_licenses.movementMethod = LinkMovementMethod.getInstance()
        tv_licenses.text = HtmlCompat.fromHtml(getString(R.string.licenses))

        // Artwork
        tv_artwork.movementMethod = LinkMovementMethod.getInstance()
        tv_artwork.text = HtmlCompat.fromHtml(getString(R.string.artwork))

        val drawableId = if (isDarkTheme)
            R.drawable.im_pixplicity_white
        else
            R.drawable.im_pixplicity_color

        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        // drawable = VectorDrawableCompat.create(getResources(), drawableId, getActivity().getTheme());

        iv_pixplicity.setImageDrawable(drawable)

        // Website
        val launchWebsite = View.OnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(getString(R.string.url_pixplicity))
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(context, R.string.error_no_activity, Toast.LENGTH_LONG).show()
            }
        }
        bt_website.setOnClickListener(launchWebsite)
        iv_pixplicity.setOnClickListener(launchWebsite)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.action_feedback -> {
                run {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", FEEDBACK_EMAIL, null))
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
                    val ime = SimpleInputConnection.getIme(context!!)
                    val keyboardPackageName = if (ime == null) "unknown" else ime.packageName
                    intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_body, versionString, keyboardPackageName))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.error_no_activity, Toast.LENGTH_LONG).show()
                    }
                }
                return true
            }
            R.id.action_rate -> {
                run {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(getString(R.string.url_google_play, context!!.packageName))
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.error_no_activity, Toast.LENGTH_LONG).show()
                    }
                }
                return true
            }
            R.id.action_beta -> {
                run {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(getString(R.string.url_beta, context!!.packageName))
                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, R.string.error_no_activity, Toast.LENGTH_LONG).show()
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
