package com.pixplicity.cryptogram.fragments

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.billingclient.api.*
import com.crashlytics.android.Crashlytics
import com.pixplicity.cryptogram.BuildConfig
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.invertedTheme
import kotlinx.android.synthetic.main.fragment_donate.*

class DonateFragment : BaseFragment(), PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient

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
                    val skuList = arrayListOf("domation_1")
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build(), { responseCode, skuDetailsList ->
                        // TODO display a list of SKUs
                    })
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, { responseCode, purchasesList ->
                        if (responseCode == BillingClient.BillingResponse.OK && purchasesList != null) {
                            for (purchase in purchasesList) {
                                // TODO display list of purchases
                                when (purchase.sku) {
                                    "donation_1" -> {
                                        // ...
                                    }
                                }
                            }
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

            val dialog = AlertDialog.Builder(context)
                    .setTitle(R.string.donate_title)
                    .setMessage(R.string.donate_message)
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
            val flowParams = BillingFlowParams.newBuilder()
                    .setSku("donation_1")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            val responseCode = billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            purchases.forEach {
                billingClient.consumeAsync(it.purchaseToken, { responseCode, outToken ->
                    if (responseCode == BillingClient.BillingResponse.OK) {
                        // Handle the success of the consume operation.
                        // For example, increase the number of coins inside the user&#39;s basket.
                    }
                })
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

}
