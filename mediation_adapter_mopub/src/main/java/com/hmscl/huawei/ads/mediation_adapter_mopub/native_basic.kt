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

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.hmscl.huawei.ads.mediation_adapter_mopub.native_advanced.Companion.HTTPS_TAG
import com.hmscl.huawei.ads.mediation_adapter_mopub.native_advanced.Companion.HTTP_TAG
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.*
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.CONTENT_URL_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.KEY_EXPERIMENTAL_EXTRA_SWAP_MARGINS
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.KEY_EXTRA_AD_CHOICES_PLACEMENT
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.KEY_EXTRA_ORIENTATION_PREFERENCE
import com.huawei.hms.ads.*
import com.huawei.hms.ads.nativead.NativeAd
import com.huawei.hms.ads.nativead.NativeAdConfiguration
import com.huawei.hms.ads.nativead.NativeAdLoader
import com.mopub.common.Preconditions
import com.mopub.common.logging.MoPubLog
import com.mopub.nativeads.CustomEventNative
import com.mopub.nativeads.NativeErrorCode
import com.mopub.nativeads.NativeImageHelper
import com.mopub.nativeads.StaticNativeAd
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class native_basic : CustomEventNative() {
    private val KEY_EXTRA_AD_UNIT_ID = HuaweiAdsCustomEventDataKeys.AD_UNIT_ID_KEY
    private val ADAPTER_NAME = native_advanced::class.java.simpleName
    private val sIsInitialized = AtomicBoolean(false)
    private var mAdUnitId: String? = null
    private var mHuaweiAdapterConfiguration = HuaweiAdsAdapterConfiguration()

    override fun loadNativeAd(
            context: Context,
            customEventNativeListener: CustomEventNativeListener,
            localExtras: Map<String?, Any?>,
            serverExtras: Map<String, String>
    ) {
        Preconditions.checkNotNull(context)
        Preconditions.checkNotNull(customEventNativeListener)
        Preconditions.checkNotNull(localExtras)
        if (!sIsInitialized.getAndSet(true)) {
            HwAds.init(context)
        }
        mAdUnitId = serverExtras[KEY_EXTRA_AD_UNIT_ID]
        if (TextUtils.isEmpty(mAdUnitId)) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NETWORK_NO_FILL)
            MoPubLog.log(
                    getAdNetworkId(), MoPubLog.AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                    NativeErrorCode.NETWORK_NO_FILL.intCode,
                    NativeErrorCode.NETWORK_NO_FILL
            )
            return
        }
        val nativeAd = HuaweiNativeAd(customEventNativeListener)
        nativeAd.loadAd(context, mAdUnitId, localExtras, serverExtras)
        mHuaweiAdapterConfiguration.setCachedInitializationParameters(context, serverExtras)
    }

    inner class HuaweiNativeAd(private var mCustomEventNativeListener: CustomEventNativeListener?) :
            StaticNativeAd() {
        var shouldSwapMargins = false
        var huaweiNativeAd: NativeAd? = null

        fun loadAd(
                context: Context,
                adUnitId: String?,
                localExtras: Map<String?, Any?>,
                serverExtras: Map<String, String>
        ) {
            val builder = NativeAdLoader.Builder(context, adUnitId)
            // TODO: 9/14/2021  
            if (localExtras.containsKey(KEY_EXPERIMENTAL_EXTRA_SWAP_MARGINS)) {
                val swapMarginExtra = localExtras[KEY_EXPERIMENTAL_EXTRA_SWAP_MARGINS]
                if (swapMarginExtra is Boolean) {
                    shouldSwapMargins = swapMarginExtra
                }
            }
            val optionsBuilder = NativeAdConfiguration.Builder()
            optionsBuilder.setRequestMultiImages(false)

            /**
             * Sets the orientation of an ad image
             * https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-References-V1/nativead-cfg-builder-0000001050298606-V1
             */
            if (localExtras.containsKey(KEY_EXTRA_ORIENTATION_PREFERENCE) && isValidOrientationExtra(
                            localExtras[KEY_EXTRA_ORIENTATION_PREFERENCE]
                    )
            ) {
                optionsBuilder.setMediaDirection(
                        localExtras[KEY_EXTRA_ORIENTATION_PREFERENCE].toString().toInt()
                )
            } else if (serverExtras.containsKey(KEY_EXTRA_ORIENTATION_PREFERENCE) && isValidOrientationExtra(
                            serverExtras[KEY_EXTRA_ORIENTATION_PREFERENCE]
                    )
            ) {
                optionsBuilder.setMediaDirection(serverExtras[KEY_EXTRA_ORIENTATION_PREFERENCE]!!.toInt())
            }

            /**
             * Sets the AdChoices icon position
             * https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-References-V1/nativead-cfg-builder-0000001050298606-V1
             */
            if (localExtras.containsKey(KEY_EXTRA_AD_CHOICES_PLACEMENT) && isValidAdChoicesPlacementExtra(
                            localExtras[KEY_EXTRA_AD_CHOICES_PLACEMENT]
                    )
            ) {
                optionsBuilder.setChoicesPosition(
                        localExtras[KEY_EXTRA_AD_CHOICES_PLACEMENT].toString().toInt()
                )
            } else if (serverExtras.containsKey(KEY_EXTRA_AD_CHOICES_PLACEMENT) && isValidAdChoicesPlacementExtra(
                            serverExtras[KEY_EXTRA_AD_CHOICES_PLACEMENT]
                    )
            ) {
                optionsBuilder.setChoicesPosition(serverExtras[KEY_EXTRA_AD_CHOICES_PLACEMENT]!!.toInt())
            }

            val adOptions = optionsBuilder.build()
            val adLoader = builder.setNativeAdLoadedListener {
                it
                if (!isValidHuaweiNativeAd(it)) {
                    MoPubLog.log(
                            getAdNetworkId(), MoPubLog.AdapterLogEvent.CUSTOM, ADAPTER_NAME,
                            "The Huawei native unified ad is missing one or " +
                                    "more required assets, failing request."
                    )
                    mCustomEventNativeListener!!.onNativeAdFailed(
                            NativeErrorCode.NETWORK_NO_FILL
                    )
                    MoPubLog.log(
                            getAdNetworkId(), MoPubLog.AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                            NativeErrorCode.NETWORK_NO_FILL.intCode,
                            NativeErrorCode.NETWORK_NO_FILL
                    )
                } else {
                    huaweiNativeAd = it
                    val images: List<Image> = it.images
                    val imageUrls: MutableList<String> = ArrayList()
                    val mainImage: Image = images[0]
                    imageUrls.add(mainImage.uri.toString().replace(HTTP_TAG, HTTPS_TAG))
                    if (it.icon != null) {
                        val iconImage: Image = it.icon
                        // Assuming that the URI provided is an URL.
                        imageUrls.add(iconImage.uri.toString().replace(HTTP_TAG, HTTPS_TAG))
                    }
                    preCacheImages(context, imageUrls)
                }
            }.setAdListener(object : AdListener() {
                override fun onAdClicked() {
                    super.onAdClicked()
                    notifyAdClicked()
                    MoPubLog.log(getAdNetworkId(), MoPubLog.AdapterLogEvent.CLICKED, ADAPTER_NAME)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                    notifyAdImpressed()
                    MoPubLog.log(
                            getAdNetworkId(),
                            MoPubLog.AdapterLogEvent.SHOW_SUCCESS,
                            ADAPTER_NAME
                    )
                }

                override fun onAdFailed(loadAdError: Int) {
                    MoPubLog.log(
                            getAdNetworkId(), MoPubLog.AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                            NativeErrorCode.NETWORK_NO_FILL.intCode,
                            NativeErrorCode.NETWORK_NO_FILL
                    )
                    MoPubLog.log(
                            getAdNetworkId(),
                            MoPubLog.AdapterLogEvent.CUSTOM,
                            ADAPTER_NAME,
                            "Failed to " +
                                    "load Huawei native ad with message: " + loadAdError +
                                    ". Caused by: " + loadAdError
                    )
                    when (loadAdError) {
                        AdParam.ErrorCode.INNER -> mCustomEventNativeListener!!.onNativeAdFailed(
                                NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR
                        )
                        AdParam.ErrorCode.INVALID_REQUEST -> mCustomEventNativeListener!!.onNativeAdFailed(
                                NativeErrorCode.NETWORK_INVALID_REQUEST
                        )
                        AdParam.ErrorCode.NETWORK_ERROR -> mCustomEventNativeListener!!.onNativeAdFailed(
                                NativeErrorCode.CONNECTION_ERROR
                        )
                        AdParam.ErrorCode.NO_AD -> mCustomEventNativeListener!!.onNativeAdFailed(
                                NativeErrorCode.NETWORK_NO_FILL
                        )
                        else -> mCustomEventNativeListener!!.onNativeAdFailed(
                                NativeErrorCode.UNSPECIFIED
                        )
                    }
                }
            }).setNativeAdOptions(adOptions).build()

            val requestBuilder = AdParam.Builder()
            requestBuilder.setRequestOrigin("MoPub")

            // Publishers may append a content URL by passing it to the MoPubNative.setLocalExtras() call.
            val contentUrl = localExtras[CONTENT_URL_KEY] as String?
            if (!TextUtils.isEmpty(contentUrl)) {
                requestBuilder.setTargetingContentUrl(contentUrl)
            } else if (!TextUtils.isEmpty(serverExtras[CONTENT_URL_KEY])) {
                requestBuilder.setTargetingContentUrl(serverExtras[CONTENT_URL_KEY])
            }

            /**
             * Prepare Child-protection keys
             */
            val requestConfigurationBuilder = prepareBuilderViaExtras(serverExtras)
            val requestConfiguration = requestConfigurationBuilder.build()
            HwAds.setRequestOptions(requestConfiguration)

            val adRequest = requestBuilder.build()
            adLoader.loadAd(adRequest)
            MoPubLog.log(getAdNetworkId(), MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED, ADAPTER_NAME)
        }

        override fun prepare(view: View) {
            view.setOnClickListener {
                notifyAdClicked()
                huaweiNativeAd?.triggerClick(Bundle.EMPTY)
                MoPubLog.log(getAdNetworkId(), MoPubLog.AdapterLogEvent.CLICKED, ADAPTER_NAME)
            }
        }

        override fun clear(view: View) {
            mCustomEventNativeListener = null
        }

        override fun destroy() {
            huaweiNativeAd?.destroy()
        }

        override fun handleClick(view: View) {
            notifyAdClicked()
            MoPubLog.log(getAdNetworkId(), MoPubLog.AdapterLogEvent.CLICKED, ADAPTER_NAME)
        }

        private fun preCacheImages(context: Context, imageUrls: List<String>) {
            NativeImageHelper.preCacheImages(context, imageUrls,
                    object : NativeImageHelper.ImageListener {
                        override fun onImagesCached() {
                            if (huaweiNativeAd != null) {
                                prepareHuaweiNativeAd(huaweiNativeAd!!)
                                mCustomEventNativeListener!!.onNativeAdLoaded(this@HuaweiNativeAd)
                                MoPubLog.log(
                                        getAdNetworkId(),
                                        MoPubLog.AdapterLogEvent.LOAD_SUCCESS,
                                        ADAPTER_NAME
                                )
                            }
                        }

                        override fun onImagesFailedToCache(errorCode: NativeErrorCode) {
                            mCustomEventNativeListener!!.onNativeAdFailed(errorCode)
                            MoPubLog.log(
                                    getAdNetworkId(), MoPubLog.AdapterLogEvent.LOAD_FAILED, ADAPTER_NAME,
                                    errorCode.intCode,
                                    errorCode
                            )
                        }
                    })
        }

        private fun prepareHuaweiNativeAd(mHuaweiNativeAd: NativeAd) {
            val images: List<Image> = mHuaweiNativeAd.images
            mainImageUrl = images[0].uri.toString().replace(HTTP_TAG, HTTPS_TAG)
            if (mHuaweiNativeAd.icon != null) {
                val icon = mHuaweiNativeAd.icon
                iconImageUrl = icon.uri.toString().replace(HTTP_TAG, HTTPS_TAG)
            }
            callToAction = mHuaweiNativeAd.callToAction
            title = mHuaweiNativeAd.title
            text = mHuaweiNativeAd.description
        }
    }

    private fun getAdNetworkId(): String? {
        return mAdUnitId
    }
}