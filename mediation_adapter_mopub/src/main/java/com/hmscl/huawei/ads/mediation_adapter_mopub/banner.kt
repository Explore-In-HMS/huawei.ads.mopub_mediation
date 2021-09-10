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
import android.view.View
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys
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

class banner : BaseAd() {
    val AD_UNIT_ID_KEY = HuaweiAdsCustomEventDataKeys.AD_UNIT_ID_KEY
    val CONTENT_URL_KEY = HuaweiAdsCustomEventDataKeys.CONTENT_URL_KEY
    val ADAPTER_NAME = banner::class.java.simpleName
    private lateinit var mHuaweiAdView: BannerView
    private var mAdUnitId: String? = null
    private var adWidth: Int? = null
    private var adHeight: Int? = null

    override fun load(context: Context, adData: AdData) {
        Preconditions.checkNotNull(context)
        Preconditions.checkNotNull(adData)
        adWidth = adData.adWidth
        adHeight = adData.adHeight
        val extras = adData.extras
        mAdUnitId = extras[AD_UNIT_ID_KEY]

        mHuaweiAdView = BannerView(context)
        mHuaweiAdView.adListener = AdViewListener()
        mHuaweiAdView.adId = mAdUnitId

        val adSize: BannerAdSize? =
                if (adWidth == null || adHeight == null || adWidth!! <= 0 || adHeight!! <= 0) null else BannerAdSize(
                        adWidth!!,
                        adHeight!!
                )

        if (adSize != null) {
            mHuaweiAdView.bannerAdSize = adSize
        } else {
            MoPubLog.log(
                    adNetworkId,
                    AdapterLogEvent.LOAD_FAILED,
                    ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.intCode,
                    MoPubErrorCode.NETWORK_NO_FILL
            )

            if (mLoadListener != null) {
                mLoadListener.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL)
            }
            return
        }

        val builder = AdParam.Builder()
        builder.setRequestOrigin("MoPub")

        val contentUrl = extras[CONTENT_URL_KEY]

        if (!TextUtils.isEmpty(contentUrl)) {
            builder.setTargetingContentUrl(contentUrl)
        }

        val requestConfigurationBuilder = prepareBuilderViaExtras(extras)

        val requestConfiguration = requestConfigurationBuilder.build()
        HwAds.setRequestOptions(requestConfiguration)
        val adRequest = builder.build()
        mHuaweiAdView.loadAd(adRequest)
        MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_ATTEMPTED, ADAPTER_NAME)
    }

    override fun getAdView(): View? {
        return mHuaweiAdView
    }

    override fun onInvalidate() {
        Views.removeFromParent(mHuaweiAdView)
        mHuaweiAdView.adListener = null
        mHuaweiAdView.destroy()
    }

    override fun getLifecycleListener(): LifecycleListener? {
        return null
    }

    override fun getAdNetworkId(): String {
        return if (mAdUnitId == null) "" else mAdUnitId!!
    }

    override fun checkAndInitializeSdk(
            launcherActivity: Activity,
            adData: AdData
    ): kotlin.Boolean {
        return false
    }

    private inner class AdViewListener : AdListener() {
        override fun onAdClosed() {
            super.onAdClosed()
        }

        override fun onAdFailed(loadAdError: Int) {
            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    getMoPubErrorCode(loadAdError)!!.intCode,
                    getMoPubErrorCode(loadAdError))
            MoPubLog.log(adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Failed to load Huawei " +
                    "banners with message: " + loadAdError + ". Caused by: " +
                    loadAdError)

            if (mLoadListener != null) {
                mLoadListener.onAdLoadFailed(getMoPubErrorCode(loadAdError)!!)
            }
        }

        override fun onAdLeave() {
            super.onAdLeave()
        }

        override fun onAdOpened() {
            MoPubLog.log(adNetworkId, AdapterLogEvent.CLICKED, ADAPTER_NAME)

            if (mInteractionListener != null) {
                mInteractionListener.onAdClicked()
            }
        }

        override fun onAdLoaded() {
            val receivedWidth: Int = mHuaweiAdView.bannerAdSize.width
            val receivedHeight: Int = mHuaweiAdView.bannerAdSize.height

            if (receivedWidth > adWidth!! || receivedHeight > adHeight!!) {
                MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                        MoPubErrorCode.NETWORK_NO_FILL.intCode, MoPubErrorCode.NETWORK_NO_FILL
                )
                MoPubLog.log(adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Huawei served an ad but" +
                        " it was invalidated because its size of " + receivedWidth + " x " + receivedHeight +
                        " exceeds the publisher-specified size of " + adWidth + " x " + adHeight)
                if (mLoadListener != null) {
                    mLoadListener.onAdLoadFailed(getMoPubErrorCode(MoPubErrorCode.NETWORK_NO_FILL.intCode)!!)
                }
            } else {
                MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_SUCCESS, ADAPTER_NAME)
                MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_ATTEMPTED, ADAPTER_NAME)
                MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_SUCCESS, ADAPTER_NAME)
                if (mLoadListener != null) {
                    mLoadListener.onAdLoaded()
                }
            }
        }

        override fun onAdClicked() {
            MoPubLog.log(adNetworkId, AdapterLogEvent.CLICKED, ADAPTER_NAME)

            if (mInteractionListener != null) {
                mInteractionListener.onAdClicked()
            }
        }

        override fun onAdImpression() {
            super.onAdImpression()
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