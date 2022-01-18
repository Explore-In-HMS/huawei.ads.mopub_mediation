/*
 * Copyright 2021. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hmscl.huawei.ads.mediation_adapter_mopub

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsAdapterConfiguration
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.AD_UNIT_ID_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.CONTENT_URL_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.prepareBuilderViaExtras
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.InterstitialAd
import com.mopub.common.LifecycleListener
import com.mopub.common.Preconditions
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.AdapterLogEvent
import com.mopub.mobileads.AdData
import com.mopub.mobileads.BaseAd
import com.mopub.mobileads.MoPubErrorCode
import java.io.PrintWriter
import java.io.StringWriter

class interstitial : BaseAd() {
    private val ADAPTER_NAME: String = interstitial::class.java.getSimpleName()
    private var mHuaweiAdsAdapterConfiguration = HuaweiAdsAdapterConfiguration()
    private var mHuaweiInterstitialAd: InterstitialAd? = null
    private var mAdUnitId: String? = null

    override fun load(context: Context, adData: AdData) {
        Log.d("TAG", "Interstitial - load()")

        try {
            Preconditions.checkNotNull(context)
            Preconditions.checkNotNull(adData)
            setAutomaticImpressionAndClickTracking(false)
            val extras = adData.extras

            if (extras.isNullOrEmpty()) {
                Log.d("TAG", "Interstitial - load() - adData.extras is empty or null")

                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR.intCode,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR
                )
            }

            if (extras.containsKey(AD_UNIT_ID_KEY)) {
                mAdUnitId = extras[AD_UNIT_ID_KEY]
                mHuaweiAdsAdapterConfiguration.setCachedInitializationParameters(context, extras)

                Log.d("TAG", "Interstitial - load() - adData.extras => {adUnitID = $mAdUnitId}")
            } else {
                Log.e("TAG", "Interstitial - load() - adData.extras is not contain adUnitID")
                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.intCode,
                    MoPubErrorCode.NETWORK_NO_FILL
                )
                mLoadListener?.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL)
                return
            }
            mHuaweiInterstitialAd = InterstitialAd(context)
            mHuaweiInterstitialAd!!.adListener = InterstitialAdListener()
            mHuaweiInterstitialAd!!.adId = mAdUnitId
            val builder = AdParam.Builder()
            builder.setRequestOrigin("MoPub")

            if (!extras.containsKey(CONTENT_URL_KEY)) {
                Log.e("TAG", "Interstitial - load() - adData.extras is not contain contentUrl")
            }

            val contentUrl = extras[CONTENT_URL_KEY]

            if (!TextUtils.isEmpty(contentUrl)) {
                builder.setTargetingContentUrl(contentUrl)
                Log.d("TAG", "Interstitial - load() - adData.extras => {contentUrl = $contentUrl}")
            } else {
                Log.e("TAG", "Interstitial - load() - adData.extras => contentUrl key is empty")
            }

            /**
             * Prepare Child-protection keys
             */
            val requestConfigurationBuilder = prepareBuilderViaExtras(extras)
            val requestConfiguration = requestConfigurationBuilder.build()
            HwAds.setRequestOptions(requestConfiguration)
            val adRequest = builder.build()

            mHuaweiInterstitialAd!!.loadAd(adRequest)
            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_ATTEMPTED, ADAPTER_NAME)
            Log.d("TAG", "Interstitial - load() - adapter attempting to load ad")

        } catch (e: Exception) {
            val stacktrace =
                StringWriter().also { e.printStackTrace(PrintWriter(it)) }.toString().trim()
            Log.e("TAG", "Interstitial - loadAd() - Request Interstitial Ad Failed: $stacktrace")
            mHuaweiInterstitialAd!!.adListener.onAdFailed(AdParam.ErrorCode.INNER)
        }

    }

    override fun show() {
        Log.d("TAG", "Interstitial - show()")

        MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_ATTEMPTED, ADAPTER_NAME)
        if (mHuaweiInterstitialAd!!.isLoaded) {
            Log.d("TAG", "Interstitial - show() - InterstitialAd isLoaded true")
            mHuaweiInterstitialAd!!.show()
        } else {
            Log.e("TAG", "Interstitial - show() - InterstitialAd isLoaded false")
            MoPubLog.log(
                adNetworkId, AdapterLogEvent.SHOW_FAILED, ADAPTER_NAME,
                MoPubErrorCode.NETWORK_NO_FILL.intCode,
                MoPubErrorCode.NETWORK_NO_FILL
            )
            mInteractionListener?.onAdFailed(MoPubErrorCode.NETWORK_NO_FILL)
        }
    }

    override fun onInvalidate() {
        Log.d("TAG", "Interstitial - onInvalidate()")
        if (mHuaweiInterstitialAd != null) {
            Log.d("TAG", "Interstitial - onInvalidate() - InterstitialAd is not null")
            mHuaweiInterstitialAd!!.adListener = null
            mHuaweiInterstitialAd = null
        } else {
            Log.d("TAG", "Interstitial - onInvalidate() - InterstitialAd is null")
        }
    }

    override fun getLifecycleListener(): LifecycleListener? {
        Log.d("TAG", "Interstitial - getLifecycleListener()")
        return null
    }

    override fun getAdNetworkId(): String {
        Log.d("TAG", "Interstitial - getAdNetworkId()")
        return if (mAdUnitId == null) {
            Log.d("TAG", "Interstitial - getAdNetworkId() - mAdUnitId is null")
            ""
        } else {
            mAdUnitId!!
        }
    }

    override fun checkAndInitializeSdk(
        launcherActivity: Activity,
        adData: AdData
    ): Boolean {
        Log.d("TAG", "Interstitial - checkAndInitializeSdk()")
        return false
    }

    private inner class InterstitialAdListener : AdListener() {
        override fun onAdClosed() {
            Log.d("TAG", "Interstitial - InterstitialAdListener - onAdClosed()")
            mInteractionListener?.onAdDismissed()
        }

        override fun onAdFailed(loadAdError: Int) {

            Log.e(
                "TAG",
                "Interstitial - InterstitialAdListener - onAdFailed - Failed to load Huawei interstitial with message: ${
                    getMoPubErrorCode(
                        loadAdError
                    )!!.name
                }. Caused by: $loadAdError"
            )

            MoPubLog.log(
                adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                getMoPubErrorCode(loadAdError)!!.intCode,
                getMoPubErrorCode(loadAdError)
            )
            MoPubLog.log(
                adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Failed to load Huawei " +
                        "interstitial with message: " + getMoPubErrorCode(loadAdError)!!.name + ". Caused by: " +
                        loadAdError
            )

            mLoadListener?.onAdLoadFailed(getMoPubErrorCode(loadAdError)!!)
        }

        override fun onAdLeave() {
            Log.d("TAG", "Interstitial - InterstitialAdListener - onAdLeave()")
            mInteractionListener?.onAdClicked()
        }

        override fun onAdLoaded() {
            Log.d("TAG", "Interstitial - InterstitialAdListener - onAdLoaded()")
            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_SUCCESS, ADAPTER_NAME)
            mLoadListener?.onAdLoaded()
        }

        override fun onAdOpened() {
            Log.d("TAG", "Interstitial - InterstitialAdListener - onAdOpened()")
            MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_SUCCESS, ADAPTER_NAME)
            if (mInteractionListener != null) {
                mInteractionListener!!.onAdShown()
                mInteractionListener!!.onAdImpression()
            }
        }

        private fun getMoPubErrorCode(error: Int): MoPubErrorCode? {
            return when (error) {
                AdParam.ErrorCode.INNER -> MoPubErrorCode.INTERNAL_ERROR
                AdParam.ErrorCode.INVALID_REQUEST -> MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR
                AdParam.ErrorCode.NETWORK_ERROR -> MoPubErrorCode.NO_CONNECTION
                AdParam.ErrorCode.NO_AD -> MoPubErrorCode.NO_FILL
                else -> MoPubErrorCode.UNSPECIFIED
            }
        }


    }
}