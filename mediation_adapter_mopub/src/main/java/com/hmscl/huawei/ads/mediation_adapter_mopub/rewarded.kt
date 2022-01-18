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
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.KEY_EXTRA_APPLICATION_ID
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.prepareBuilderViaExtras
import com.huawei.hms.ads.AdParam
import com.huawei.hms.ads.HwAds
import com.huawei.hms.ads.reward.Reward
import com.huawei.hms.ads.reward.RewardAd
import com.huawei.hms.ads.reward.RewardAdLoadListener
import com.huawei.hms.ads.reward.RewardAdStatusListener
import com.mopub.common.LifecycleListener
import com.mopub.common.MoPubReward
import com.mopub.common.Preconditions
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.AdapterLogEvent
import com.mopub.mobileads.AdData
import com.mopub.mobileads.BaseAd
import com.mopub.mobileads.MoPubErrorCode
import java.io.PrintWriter
import java.io.StringWriter
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class rewarded : BaseAd() {
    private val ADAPTER_NAME: String = rewarded::class.java.getSimpleName()
    private var sIsInitialized = AtomicBoolean(false)
    private var mAdUnitId: String? = null
    private var mRewardedAd: RewardAd? = null
    private var mIsLoaded = false
    private var mWeakActivity: WeakReference<Activity?>? = null
    private var mHuaweiAdsAdapterConfiguration = HuaweiAdsAdapterConfiguration()

    override fun getLifecycleListener(): LifecycleListener? {
        Log.d(ADAPTER_NAME, "Rewarded - getLifecycleListener()")
        return null
    }

    override fun getAdNetworkId(): String {
        Log.d(ADAPTER_NAME, "Rewarded - getAdNetworkId()")
        return if (mAdUnitId == null) "" else mAdUnitId!!
    }

    override fun onInvalidate() {
        Log.d(ADAPTER_NAME, "Rewarded - onInvalidate()")
        if (mRewardedAd != null) {
            mRewardedAd = null
        }
    }

    @Throws(Exception::class)
    override fun checkAndInitializeSdk(
        launcherActivity: Activity,
        adData: AdData
    ): Boolean {
        Log.d(ADAPTER_NAME, "Rewarded - checkAndInitializeSdk()")

        Preconditions.checkNotNull(launcherActivity)
        Preconditions.checkNotNull(adData)
        if (!sIsInitialized!!.getAndSet(true)) {
            val extras = adData.extras

            if (extras.isNullOrEmpty()) {
                Log.e(ADAPTER_NAME, "Rewarded - checkAndInitializeSdk() - adData.extras is empty or null")
            }

            if (!extras.containsKey(KEY_EXTRA_APPLICATION_ID)) {
                Log.e(
                    ADAPTER_NAME,
                    "Rewarded - checkAndInitializeSdk() - adData.extras is not contain appid"
                )
            }
            if (TextUtils.isEmpty(extras[KEY_EXTRA_APPLICATION_ID])) {
                Log.e(
                    ADAPTER_NAME,
                    "Rewarded - checkAndInitializeSdk() - adData.extras => appid key is empty"
                )
                HwAds.init(launcherActivity)
            } else {
                Log.d(
                    ADAPTER_NAME,
                    "Rewarded - checkAndInitializeSdk() - adData.extras => {appid = ${extras[KEY_EXTRA_APPLICATION_ID]}}"
                )
                HwAds.init(launcherActivity, extras[KEY_EXTRA_APPLICATION_ID])
            }

            if (!extras.containsKey(AD_UNIT_ID_KEY)) {
                Log.e(
                    ADAPTER_NAME,
                    "Rewarded - checkAndInitializeSdk() - adData.extras is not contain adUnitID"
                )
            }
            mAdUnitId = extras[AD_UNIT_ID_KEY]
            if (TextUtils.isEmpty(mAdUnitId)) {
                Log.e(
                    ADAPTER_NAME,
                    "Rewarded - checkAndInitializeSdk() - adData.extras => adUnitID key is empty"
                )
                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.NETWORK_NO_FILL.intCode,
                    MoPubErrorCode.NETWORK_NO_FILL
                )
                mLoadListener?.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL)
                return false
            } else {
                Log.d(
                    ADAPTER_NAME,
                    "Rewarded - checkAndInitializeSdk() - adData.extras => {adUnitID = ${extras[AD_UNIT_ID_KEY]}}"
                )
            }
            mHuaweiAdsAdapterConfiguration.setCachedInitializationParameters(
                launcherActivity,
                extras
            )
            return true
        }
        return false
    }

    override fun load(context: Context, adData: AdData) {

        try{
            Log.d(ADAPTER_NAME, "Rewarded - load()")

            setAutomaticImpressionAndClickTracking(false)
            val extras = adData.extras

            if (extras.isNullOrEmpty()) {
                Log.e(ADAPTER_NAME, "Rewarded - load() - adData.extras is empty or null")
            }

            if (!extras.containsKey(AD_UNIT_ID_KEY)) {
                Log.e(ADAPTER_NAME, "Rewarded - load() - adData.extras is not contain adUnitID")
            }

            mAdUnitId = extras[AD_UNIT_ID_KEY]!!
            if (TextUtils.isEmpty(mAdUnitId)) {
                Log.e(ADAPTER_NAME, "Rewarded - load() - adData.extras => adUnitID key is empty")
                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR.intCode,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR
                )
                mLoadListener?.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR)
                return
            } else {
                Log.d(ADAPTER_NAME, "Rewarded - load() - adData.extras => {adUnitID = $mAdUnitId}")
            }

            if (context !is Activity) {
                Log.e(
                    ADAPTER_NAME,
                    "Rewarded - load() - Context passed to load was not an activity. This is a bug in MoPub"
                )
                MoPubLog.log(
                    adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Context passed to load " +
                            "was not an Activity. This is a bug in MoPub."
                )
                mLoadListener?.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR)
                return
            }
            mWeakActivity = WeakReference(context)
            mRewardedAd = RewardAd(context, mAdUnitId)
            val builder = AdParam.Builder()
            builder.setRequestOrigin("MoPub")

            if (!extras.containsKey(CONTENT_URL_KEY)) {
                Log.e(ADAPTER_NAME, "Rewarded - load() - adData.extras is not contain contentUrl")
            }

            val contentUrl = extras[CONTENT_URL_KEY]
            if (!TextUtils.isEmpty(contentUrl)) {
                Log.d(ADAPTER_NAME, "Rewarded - load() - adData.extras => {contentUrl = $contentUrl}")
                builder.setTargetingContentUrl(contentUrl)
            } else {
                Log.e(ADAPTER_NAME, "Rewarded - load() - adData.extras => contentUrl key is empty")
            }

            /**
             * Prepare Child-protection keys
             */
            val requestConfigurationBuilder = prepareBuilderViaExtras(extras)
            val requestConfiguration = requestConfigurationBuilder.build()
            HwAds.setRequestOptions(requestConfiguration)

            val adRequest = builder.build()
            mRewardedAd!!.loadAd(adRequest, mRewardedAdLoadCallback)
            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_ATTEMPTED, ADAPTER_NAME)
            Log.d(ADAPTER_NAME, "Rewarded - load() - adapter attempting to load ad")
        }catch (e: Exception) {
            val stacktrace =
                StringWriter().also { e.printStackTrace(PrintWriter(it)) }.toString().trim()
            Log.e(ADAPTER_NAME, "Native Basic - loadNativeAd() - Request Native Ad Failed: $stacktrace")
            mRewardedAd?.rewardAdListener?.onRewardAdFailedToLoad(AdParam.ErrorCode.INNER)
        }

    }

    private fun hasVideoAvailable(): Boolean {
        Log.d(
            ADAPTER_NAME,
            "Rewarded - hasVideoAvailable() : ${(mRewardedAd != null && mIsLoaded).toString()}"
        )
        return mRewardedAd != null && mIsLoaded
    }

    override fun show() {
        Log.d(ADAPTER_NAME, "Rewarded - show()")
        MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_ATTEMPTED, ADAPTER_NAME)
        if (hasVideoAvailable() && mWeakActivity != null && mWeakActivity!!.get() != null) {
            mRewardedAd!!.show(mWeakActivity!!.get(), mRewardedAdCallback)
            Log.d(ADAPTER_NAME, "Rewarded - show() - Rewarded ad showed successfully")
        } else {
            MoPubLog.log(
                adNetworkId, AdapterLogEvent.SHOW_FAILED, ADAPTER_NAME,
                MoPubErrorCode.NETWORK_NO_FILL.intCode,
                MoPubErrorCode.NETWORK_NO_FILL
            )
            mInteractionListener?.onAdFailed(getMoPubErrorCode(AdParam.ErrorCode.NO_AD)!!)
            Log.e(ADAPTER_NAME, "Rewarded - show() - Rewarded ad show failed")
        }
    }

    private val mRewardedAdLoadCallback = object : RewardAdLoadListener() {
        override fun onRewardAdFailedToLoad(loadAdError: Int) {
            Log.e(
                ADAPTER_NAME,
                "Rewarded - RewardAdLoadListener - onRewardAdFailedToLoad() - Failed to load Huawei rewarded with loadError: $loadAdError"
            )

            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME)
            MoPubLog.log(
                adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Failed to load Huawei " +
                        "rewarded video with message: " + loadAdError + ". Caused by: " +
                        loadAdError
            )
            mLoadListener?.onAdLoadFailed(getMoPubErrorCode(loadAdError)!!)
        }

        override fun onRewardedLoaded() {
            Log.d(ADAPTER_NAME, "Rewarded - RewardAdLoadListener - onRewardedLoaded()")

            mIsLoaded = true
            MoPubLog.log(adNetworkId, AdapterLogEvent.LOAD_SUCCESS, ADAPTER_NAME)
            mLoadListener?.onAdLoaded()
        }
    }

    private val mRewardedAdCallback = object : RewardAdStatusListener() {
        override fun onRewardAdOpened() {
            Log.d(ADAPTER_NAME, "Rewarded - RewardAdStatusListener - onRewardAdOpened()")

            MoPubLog.log(adNetworkId, AdapterLogEvent.SHOW_SUCCESS, ADAPTER_NAME)
            if (mInteractionListener != null) {
                mInteractionListener!!.onAdShown()
                mInteractionListener!!.onAdImpression()
            }
        }

        override fun onRewardAdClosed() {
            Log.d(ADAPTER_NAME, "Rewarded - RewardAdStatusListener - onRewardAdClosed()")

            MoPubLog.log(adNetworkId, AdapterLogEvent.DID_DISAPPEAR, ADAPTER_NAME)
            mInteractionListener?.onAdDismissed()
        }

        override fun onRewarded(rewardItem: Reward) {
            Log.d(ADAPTER_NAME, "Rewarded - RewardAdStatusListener - onRewarded()")

            MoPubLog.log(
                adNetworkId, AdapterLogEvent.SHOULD_REWARD, ADAPTER_NAME,
                rewardItem.amount, rewardItem.name
            )
            mInteractionListener?.onAdComplete(
                MoPubReward.success(
                    rewardItem.name,
                    rewardItem.amount
                )
            )
        }

        override fun onRewardAdFailedToShow(loadAdError: Int) {
            Log.e(
                ADAPTER_NAME,
                "Rewarded - RewardAdStatusListener - onRewardAdFailedToShow() - Failed to load Huawei rewarded video with message ${
                    getMoPubErrorCode(loadAdError)!!.name
                }. Caused by: $loadAdError"
            )

            MoPubLog.log(
                adNetworkId, AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                getMoPubErrorCode(loadAdError)!!.intCode,
                getMoPubErrorCode(loadAdError)
            )
            MoPubLog.log(
                adNetworkId, AdapterLogEvent.CUSTOM, ADAPTER_NAME, "Failed to load Huawei " +
                        "rewarded video with message: " + getMoPubErrorCode(loadAdError)!!.name + ". Caused by: " +
                        loadAdError
            )

            mLoadListener?.onAdLoadFailed(getMoPubErrorCode(loadAdError)!!)
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