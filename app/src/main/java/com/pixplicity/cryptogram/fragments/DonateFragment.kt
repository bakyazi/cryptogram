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
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.android.billingclient.api.*
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.PurchaseEvent
import com.pixplicity.cryptogram.BuildConfig
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.utils.PrefsUtils
import com.pixplicity.cryptogram.utils.donationError
import com.pixplicity.cryptogram.utils.donationThankYou
import com.pixplicity.cryptogram.utils.invertedTheme
import kotlinx.android.synthetic.main.fragment_donate.*
import org.json.JSONException
import java.math.BigDecimal
import java.text.DateFormat
import java.util.*

class DonateFragment : BaseFragment(), PurchasesUpdatedListener {

    companion object {
        private val TAG = DonateFragment::class.java.simpleName
    }

    private val skuList = arrayListOf("donation_1")
    private val skus = HashMap<String, SkuDetails>()
    private var purchases = ArrayList<Purchase>()
    private var purchasesConsumed: MutableSet<String>? = null
    private lateinit var billingClient: BillingClient

    private var handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_donate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        purchasesConsumed = (PrefsUtils.purchases ?: emptySet()).toMutableSet()
        showPurchasesInitial()
        Log.d(TAG, "${purchasesConsumed?.size} purchases consumed")

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
                        showPurchases(consume = true)
                    })
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, { responseCode, purchases ->
                        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                            this@DonateFragment.purchases = ArrayList(purchases)
                            showPurchases(consume = true)
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
        when (responseCode) {
            BillingClient.BillingResponse.OK,
            BillingClient.BillingResponse.USER_CANCELED -> {
                // Ignore
            }
            else -> {
                context?.let {
                    donationError(it, "[purchase-start]", responseCode)
                }
            }
        }
        Log.d(TAG, "launchBillingFlow: $responseCode")
    }

    override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
        Log.d(TAG, "onPurchasesUpdated: $responseCode")
        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
            purchases.forEach {
                consume(it)
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

    private fun consume(purchase: Purchase, silent: Boolean = false) {
        val purchaseToken = purchase.purchaseToken
        val purchaseId = getPurchaseId(purchase)

        // Log purchase
        val purchaseEvent = PurchaseEvent()
        val sku = skus[purchase.sku]
        if (sku != null) {
            purchaseEvent
                    .putItemPrice(BigDecimal.valueOf(sku.price.toDouble()))
                    .putCurrency(Currency.getInstance(sku.priceCurrencyCode))
                    .putItemName(sku.title)
                    .putItemType(sku.type)
        }
        purchaseEvent
                .putItemId(purchaseId)
                .putSuccess(true)
        Answers.getInstance().logPurchase(purchaseEvent)
        // Note that Firebase Analytics are reported automatically

        Log.d(TAG, "consumeAsync: [...]$purchaseId")
        billingClient.consumeAsync(purchase.purchaseToken, { responseCode, _ ->
            Log.d(TAG, "consumeAsync: [...]$purchaseId; responseCode=$responseCode")
            when (responseCode) {
                BillingClient.BillingResponse.OK -> {
                    purchasesConsumed?.add(purchase.originalJson)
                    PrefsUtils.purchases = purchasesConsumed
                    if (!silent) handler.post {
                        context?.let {
                            // Display thank-you message
                            donationThankYou(it, purchaseId)
                        }
                    }
                }
                BillingClient.BillingResponse.USER_CANCELED -> {
                    // Ignore
                }
                BillingClient.BillingResponse.ITEM_NOT_OWNED -> {
                    // Already consumed
                    purchasesConsumed?.add(purchase.originalJson)
                    PrefsUtils.purchases = purchasesConsumed
                }
                else ->
                    if (!silent) handler.post {
                        context?.let {
                            donationError(it, purchaseToken, responseCode)
                        }
                    }
            }
        })
    }

    private fun getPurchaseId(purchase: Purchase): String {
        val orderIdShort = purchase.orderId.takeLast(10)
        val purchaseTokenShort = purchase.purchaseToken.takeLast(9)
        return if (orderIdShort.isEmpty()) purchaseTokenShort else orderIdShort
    }

    private fun showPurchasesInitial() {
        if (purchases.isEmpty()) {
            purchasesConsumed?.forEach {
                try {
                    purchases.add(Purchase(it, null))
                } catch (ignore: JSONException) {
                }
            }
            showPurchases()
        }
    }

    private fun showPurchases(consume: Boolean = false) {
        tv_donations.visibility = if (purchases.isEmpty()) View.GONE else View.VISIBLE
        vg_donations.visibility = if (purchases.isEmpty()) View.GONE else View.VISIBLE
        vg_donations.removeAllViews()
        val df = DateFormat.getDateInstance(DateFormat.LONG)
        for (purchase in purchases) {
            Log.d(TAG, "showPurchases: ${purchase.originalJson}")
            val purchaseId = getPurchaseId(purchase)
            if (consume && purchase.purchaseToken != null && purchasesConsumed?.contains(purchase.originalJson) != true) {
                // Purchase hasn't been consumed yet
                consume(purchase, silent = true)
            }
            val vg_donation = layoutInflater.inflate(R.layout.item_donation, null) as ViewGroup
            val tv_donation = vg_donation.findViewById<TextView>(R.id.tv_donation)
            val bt_feedback = vg_donation.findViewById<ImageButton>(R.id.bt_donation_feedback)
            val sku = skus[purchase.sku]
            val description = if (sku != null) {
                sku.title
            } else {
                purchase.sku
            }
            tv_donation.text = getString(R.string.donation_list_item,
                    if (purchase.purchaseTime == 0L) "purchase.purchaseTime" else df.format(Date(purchase.purchaseTime)),
                    description, purchaseId)
            bt_feedback.setOnClickListener {
                donationThankYou(it.context, purchaseId)
            }
            if (isDarkTheme) {
                bt_feedback.invertedTheme()
            }
            vg_donations.addView(vg_donation)
        }
        vg_donations.requestLayout()

        if (consume) {
            // Remove invalid purchases from consumption list
            val removed = mutableListOf<String>()
            purchasesConsumed?.forEach {
                val consumedSignature = it
                purchases.filter { it.originalJson != consumedSignature }.forEach {
                    Log.d(TAG, "showPurchases: remove $consumedSignature")
                    removed.add(consumedSignature)
                }
            }
            if (removed.isNotEmpty()) {
                purchasesConsumed?.removeAll(removed)
                Log.d(TAG, "showPurchases: update consumed")
                PrefsUtils.purchases = purchasesConsumed
            }
        }
    }

}
