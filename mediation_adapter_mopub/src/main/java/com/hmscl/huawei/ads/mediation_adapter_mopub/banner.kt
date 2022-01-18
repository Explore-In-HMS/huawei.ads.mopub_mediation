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
import android.view.View
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.AD_UNIT_ID_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.CONTENT_URL_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.prepareBuilderViaExtras
import com.huawei.hms.ads.AdListener
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.BannerAdSize
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.banner.BannerView
import com.mopub.common.LifecycleListener
import com.mopub.common.Preconditions
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.AdapterLogEvent
import com.mopub.common.util.Views
import com.mopub.mobileads.AdData
import com.mopub.mobileads.BaseAd
import com.mopub.mobileads.MoPubErrorCode
import java.io.PrintWriter
import java.io.StringWriter

class banner : BaseAd() {
    val ADAPTER_NAME = banner::class.java.simpleName
    private lateinit var mHuaweiAdView: BannerView
    private var mAdUnitId: String? = null
    private var adWidth: Int? = null
    private var adHeight: Int? = null

    override fun load(context: Context, adData: AdData) {
        Log.d(ADAPTER_NAME, "Banner - load()")

        try {
            Preconditions.checkNotNull(context)
            Preconditions.checkNotNull(adData)
            adWidth = adData.adWidth
            adHeight = adData.adHeight
            val extras = adData.extras

            if (extras.isNullOrEmpty()) {
                Log.e(ADAPTER_NAME, "Banner - load() - adData.extras is empty or null")

                MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR.intCode,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR
                )
            }

            if(!extras.containsKey(AD_UNIT_ID_KEY)){
                Log.e(ADAPTER_NAME, "Banner - load() - adData.extras is not contain adUnitID")
            }

            mAdUnitId = extras[AD_UNIT_ID_KEY]
            Log.d(ADAPTER_NAME, "Banner - load() - adData.extras => {adUnitID = $mAdUnitId}")

            mHuaweiAdView = BannerView(context)
            mHuaweiAdView.adListener = AdViewListener()
            mHuaweiAdView.adId = mAdUnitId

            val adSize: BannerAdSize? =
                if (adWidth == null || adHeight == null || adWidth!! <= 0 || adHeight!! <= 0) null else BannerAdSize(
                    adWidth!!,
                    adHeight!!
                )

            if (adSize != null) {
                Log.d(ADAPTER_NAME, "Banner - load() - adSize width : $adWidth , adSize height $adHeight")
                mHuaweiAdView.bannerAdSize = adSize
            } else {
                Log.e(ADAPTER_NAME, "Banner - load() - adSize is null")
                MoPubLog.log(
                    adNetworkId,
                    AdapterLogEvent.LOAD_FAILED,
                    ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.intCode,
                    MoPubErrorCode.NETWORK_NO_FILL
                )
                mLoadListener?.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL)
                return
            }

            val builder = AdParam.Builder()
            builder.setRequestOrigin("MoPub")

            if(!extras.containsKey(CONTENT_URL_KEY)){
                Log.e(ADAPTER_NAME, "Banner - load() - adData.extras is not contain contentUrl")
            }

            val contentUrl = extras[CONTENT_URL_KEY]

            if (!TextUtils.isEmpty(contentUrl)) {
                builder.setTargetingContentUrl(contentUrl)
                Log.d(ADAPTER_NAME, "Banner - load() - adData.extras => {contentUrl = $contentUrl}")

            }else{
                Log.e(ADAPTER_NAME, "Banner - load() - adData.extras => contentUrl key is empty")
            }

            /**
             * Prepare Child-protection keys
             */
            val requestConfigurationBuilder = prepareBuilderViaExtras(extras)
            val requestConfiguration = requestConfigurationBuilder.build()
            HwAds.setRequestOptions(requestConfiguration)

            val adRequest = builder.build()
            mHuaweiAdView.loadAd(adRequest)
            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_ATTEMPTED, ADAPTER_NAME)
            Log.d(ADAPTER_NAME, "Banner - load() - adapter attempting to load ad")

        } catch (e: Exception) {
            val stacktrace =
                StringWriter().also { e.printStackTrace(PrintWriter(it)) }.toString().trim()
            Log.e(ADAPTER_NAME, "Banner - loadAd() - Request Banner Ad Failed: $stacktrace")
            mHuaweiAdView.adListener.onAdFailed(AdParam.ErrorCode.INNER)
        }

    }

    override fun getAdView(): View? {
        Log.d(ADAPTER_NAME, "Banner - getAdView()")
        return mHuaweiAdView
    }

    override fun onInvalidate() {
        Log.d(ADAPTER_NAME, "Banner - onInvalidate()")
        Views.removeFromParent(mHuaweiAdView)
        mHuaweiAdView.adListener = null
        mHuaweiAdView.destroy()
    }

    override fun getLifecycleListener(): LifecycleListener? {
        Log.d(ADAPTER_NAME, "Banner - getLifecycleListener()")
        return null
    }

    override fun getAdNetworkId(): String {
        Log.d(ADAPTER_NAME, "Banner - getAdNetworkId()")
        return if (mAdUnitId == null) {
            Log.d(ADAPTER_NAME, "Banner - getAdNetworkId() - mAdUnitId is null")
            ""
        } else {
            mAdUnitId!!
        }
    }

    override fun checkAndInitializeSdk(
        launcherActivity: Activity,
        adData: AdData
    ): Boolean {
        Log.d(ADAPTER_NAME, "Banner - checkAndInitializeSdk()")
        return false
    }

    private inner class AdViewListener : AdListener() {

        override fun onAdFailed(loadAdError: Int) {
            Log.e(
                ADAPTER_NAME,
                "Banner - AdViewListener - onAdFailed() - Failed to load Huawei banners with loadError: $loadAdError"
            )

            MoPubLog.log(
                adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                getMoPubErrorCode(loadAdError)!!.intCode,
                getMoPubErrorCode(loadAdError)
            )
            MoPubLog.log(
                adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Failed to load Huawei " +
                        "banners with message: " + loadAdError + ". Caused by: " +
                        loadAdError
            )

            mLoadListener?.onAdLoadFailed(getMoPubErrorCode(loadAdError)!!)
        }

        override fun onAdOpened() {
            Log.d(ADAPTER_NAME, "Banner - AdViewListener - onAdOpened()")
            MoPubLog.log(adNetworkId, AdapterLogEvent.CLICKED, ADAPTER_NAME)

            mInteractionListener?.onAdClicked()
        }

        override fun onAdLoaded() {
            Log.d(ADAPTER_NAME, "Banner - AdViewListener - onAdLoaded()")

            val receivedWidth: Int = mHuaweiAdView.bannerAdSize.width
            val receivedHeight: Int = mHuaweiAdView.bannerAdSize.height

            if (receivedWidth > adWidth!! || receivedHeight > adHeight!!) {
                Log.e(
                    ADAPTER_NAME,
                    "Banner - AdViewListener - onAdLoaded() - Huawei served an ad but it was invalidated because its size of h: $receivedWidth and w: $receivedHeight " +
                            "exceeds the publisher-specified size of w: $adWidth and h: $adHeight"
                )

                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.intCode, MoPubErrorCode.NETWORK_NO_FILL
                )
                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Huawei served an ad but" +
                            " it was invalidated because its size of " + receivedWidth + " x " + receivedHeight +
                            " exceeds the publisher-specified size of " + adWidth + " x " + adHeight
                )
                mLoadListener?.onAdLoadFailed(getMoPubErrorCode(MoPubErrorCode.NETWORK_NO_FILL.intCode)!!)
            } else {
                Log.d(ADAPTER_NAME,"Banner - AdViewListener - onAdLoaded() - Ad successfully loaded")
                MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_SUCCESS, ADAPTER_NAME)
                MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_ATTEMPTED, ADAPTER_NAME)
                MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_SUCCESS, ADAPTER_NAME)
                mLoadListener?.onAdLoaded()
            }
        }

        override fun onAdClicked() {
            Log.d(ADAPTER_NAME,"Banner - AdViewListener - onAdClicked()")
            MoPubLog.log(adNetworkId, AdapterLogEvent.CLICKED, ADAPTER_NAME)

            mInteractionListener?.onAdClicked()
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