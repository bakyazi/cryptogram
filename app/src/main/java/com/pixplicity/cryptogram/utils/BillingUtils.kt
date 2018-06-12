package com.pixplicity.cryptogram.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.afollestad.materialdialogs.MaterialDialog
import com.android.billingclient.api.*
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.PurchaseEvent
import com.pixplicity.cryptogram.R
import com.pixplicity.cryptogram.activities.DonateActivity
import java.math.BigDecimal
import java.util.*

object BillingUtils {

    private val TAG = BillingUtils::class.java.simpleName

    const val DONATION_SUGGESTION_FREQUENCY = 50
    val SKU_LIST = arrayListOf("donation_1")

    private val skus = HashMap<String, SkuDetails>()
    private var purchases = ArrayList<Purchase>()
    private var purchasesConsumed: MutableSet<String>? = null

    private lateinit var billingClient: BillingClient

    fun updatePurchases(context: Context, function: () -> Unit) {
        purchasesConsumed = (PrefsUtils.purchases ?: emptySet()).toMutableSet()
        Log.d(TAG, "updatePurchases: $purchasesConsumed")
        billingClient = BillingClient.newBuilder(context).setListener(object : PurchasesUpdatedListener {
            override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
                this@BillingUtils.purchases = ArrayList(purchases)
                handlePurchases(billingClient, consume = true)
            }
        }).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                Log.d(TAG, "updatePurchases: billing ready")
                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    // The billing client is ready; query purchases
                    val params = SkuDetailsParams.newBuilder()
                    params.setSkusList(SKU_LIST).setType(BillingClient.SkuType.INAPP)
                    billingClient.querySkuDetailsAsync(params.build(), { responseCode, skuDetailsList ->
                        skuDetailsList.forEach {
                            skus[it.sku] = it
                            Log.d(TAG, "querySkuDetailsAsync: ${it.sku}; ${it.title}; ${it.description}")
                        }
                        handlePurchases(billingClient, consume = true)
                    })
                    billingClient.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, { responseCode, purchases ->
                        if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                            this@BillingUtils.purchases = ArrayList(purchases)
                            handlePurchases(billingClient, consume = true)
                        }
                    })
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.d(TAG, "updatePurchases: billing disconnected")
            }
        })
    }

    fun getPurchaseId(purchase: Purchase): String {
        val orderIdShort = purchase.orderId.takeLast(10)
        val purchaseTokenShort = purchase.purchaseToken.takeLast(9)
        return if (orderIdShort.isEmpty()) purchaseTokenShort else orderIdShort
    }

    fun consume(billingClient: BillingClient, purchase: Purchase,
                successFunction: ((purchaseId: String) -> Unit)?,
                errorFunction: ((purchaseToken: String, responseCode: Int) -> Unit)?) {
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
                    successFunction?.invoke(purchaseId)
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
                    errorFunction?.invoke(purchaseToken, responseCode)
            }
        })
    }

    private fun handlePurchases(billingClient: BillingClient, consume: Boolean = false) {
        Log.d(TAG, "handlePurchases: ${purchases.size} purchases")
        for (purchase in purchases) {
            Log.d(TAG, "handlePurchases: ${purchase.originalJson}")
            if (consume && purchase.purchaseToken != null && purchasesConsumed?.contains(purchase.originalJson) != true) {
                // Purchase hasn't been consumed yet
                consume(billingClient, purchase, null, null)
            }
        }

        if (consume) {
            // Remove invalid purchases from consumption list
            val removed = mutableListOf<String>()
            purchasesConsumed?.forEach {
                val consumedSignature = it
                purchases.filter { it.originalJson != consumedSignature }.forEach {
                    Log.d(TAG, "handlePurchases: remove $consumedSignature")
                    removed.add(consumedSignature)
                }
            }
            purchasesConsumed?.removeAll(removed)
            Log.d(TAG, "handlePurchases: update consumed")
            PrefsUtils.purchases = purchasesConsumed
        }
    }

    fun suggestDonation(activity: Activity) {
        // Prompt user for a donations
        MaterialDialog.Builder(activity)
                .content(R.string.donate_suggest_text)
                .positiveText(R.string.donate_suggest_donate)
                .negativeText(R.string.donate_suggest_nope)
                .onPositive { _, _ -> activity.startActivity(DonateActivity.create(activity)) }
                .show()
    }

}
