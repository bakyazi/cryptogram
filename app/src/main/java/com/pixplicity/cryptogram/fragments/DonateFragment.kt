package com.pixplicity.cryptogram.fragments

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.android.billingclient.api.*
import com.crashlytics.android.Crashlytics
import com.pixplicity.cryptogram.BuildConfig
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.invertedTheme
import com.pixplicity.cryptogram.utils.sendFeedback
import kotlinx.android.synthetic.main.fragment_donate.*
import java.text.DateFormat
import java.util.*

class DonateFragment : BaseFragment(), PurchasesUpdatedListener {

    companion object {
        private val TAG = DonateFragment::class.java.simpleName
    }

    private val skuList = arrayListOf("donation_1")
    private val skus = HashMap<String, SkuDetails>()
    private var purchases = ArrayList<Purchase>()
    private lateinit var billingClient: BillingClient

    private var handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isDarkTheme) {
            bt_bitcoin.invertedTheme()
            bt_in_app_purchase.invertedTheme()
        }

        billingClient = BillingClient.newBuilder(context!!).setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready; query purchases
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build(), { responseCode, skuDetailsList ->
                        // TODO display a list of SKUs
                        skuDetailsList.forEach {
                            skus[it.sku] = it
                            Log.d(TAG, "querySkuDetailsAsync: ${it.sku}; ${it.title}; ${it.description}")
                        }
                        showPurchases()
                    })
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, { responseCode, purchases ->
                        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                            this@DonateFragment.purchases = ArrayList(purchases)
                            showPurchases()
                        }
                    })
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })

        bt_bitcoin.setOnClickListener {
            val context = context!!
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("bitcoin:" + BuildConfig.BITCOIN_ADDRESS))
            // FIXME perhaps check if a compatible app is installed
            //context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

            val dialog = AlertDialog.Builder(context)
                    .setMessage(getString(R.string.donate_bitcoin_message, BuildConfig.BITCOIN_ADDRESS))
                    .setPositiveButton(R.string.donate_copy_address) { dialogInterface, i ->
                        val clipboardManager = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                        if (clipboardManager != null) {
                            clipboardManager.primaryClip = ClipData.newPlainText("text", BuildConfig.BITCOIN_ADDRESS)
                            Toast.makeText(context, R.string.donate_copy_success, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, R.string.donate_copy_failure, Toast.LENGTH_SHORT).show()
                            Crashlytics.logException(IllegalStateException("Failed copying bitcoin address"))
                        }
                    }
                    .setNegativeButton(R.string.donate_launch_wallet) { dialog1, which ->
                        try {
                            startActivity(intent)
                        } catch (ignore: ActivityNotFoundException) {
                            val installPackageName = "de.schildbach.wallet"
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$installPackageName")))
                            } catch (ignore2: ActivityNotFoundException) {
                                try {
                                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$installPackageName")))
                                } catch (e: ActivityNotFoundException) {
                                    Toast.makeText(context, R.string.donate_launch_failure, Toast.LENGTH_SHORT).show()
                                    Crashlytics.logException(IllegalStateException("Failed launching Google Play", e))
                                }
                            }
                        }
                    }
                    .show()

            val packageManager = context.packageManager
            if (intent.resolveActivity(packageManager) == null) {
                // No intent available to handle action
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setText(R.string.install_bitcoin_wallet)
            }
        }

        bt_in_app_purchase.setOnClickListener {
            when (skuList.count()) {
                0 -> {
                    // Nothing to purchase
                }
                1 -> {
                    // Only one to purchase
                    doInAppPurchase(skuList.first())
                }
                else -> {
                    // FIXME display list
                    doInAppPurchase(skuList.first())
                }
            }
        }
    }

    private fun doInAppPurchase(sku: String) {
        val flowParams = BillingFlowParams.newBuilder()
                .setSku(sku)
                .setType(BillingClient.SkuType.INAPP)
                .build()
        val responseCode = billingClient.launchBillingFlow(activity, flowParams)
        Log.d(TAG, "launchBillingFlow: $responseCode")
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: $responseCode")
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            purchases.forEach {
                Log.d(TAG, "consumeAsync: ${it.purchaseToken}")
                billingClient.consumeAsync(it.purchaseToken, { responseCode, outToken ->
                    Log.d(TAG, "consumeAsync: ${it.purchaseToken}; responseCode= $responseCode")
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        handler.post {
                            // Display thank-you message
                            if (context != null) {
                                AlertDialog.Builder(context!!)
                                        .setMessage(R.string.donate_thank_you)
                                        .setPositiveButton(R.string.donate_thank_you_feedback, { dialog, _ ->
                                            sendFeedback(context!!, it.purchaseToken.takeLast(8))
                                            dialog.dismiss()
                                        })
                                        .setNegativeButton(R.string.donate_thank_you_continue, { dialog, _ ->
                                            dialog.dismiss()
                                        })
                                        .show()
                            }
                        }
                    }
                })
            }
            this.purchases = ArrayList(purchases)
            handler.post {
                showPurchases()
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private fun showPurchases() {
        tv_donations.visibility = if (purchases.isEmpty()) View.GONE else View.VISIBLE
        vg_donations.visibility = if (purchases.isEmpty()) View.GONE else View.VISIBLE
        vg_donations.removeAllViews()
        val df = DateFormat.getDateInstance(DateFormat.LONG)
        for (purchase in purchases) {
            Log.d(TAG, "queryPurchaseHistoryAsync: ${purchase.originalJson}")
            val tv_donation = layoutInflater.inflate(R.layout.li_donation, null) as TextView
            val sku = skus[purchase.sku]
            val description = if (sku != null) {
                sku.title
            } else {
                purchase.sku
            }
            tv_donation.text = df.format(Date(purchase.purchaseTime)) + ": " + description
            vg_donations.addView(tv_donation)
        }
    }

}
