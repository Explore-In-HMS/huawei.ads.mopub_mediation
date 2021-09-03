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

package com.hmscl.huawei.ads.mediation_adapter_mopub.utils

import android.content.Context
import android.text.TextUtils
import com.huawei.hms.ads.HwAds
import com.mopub.common.BaseAdapterConfiguration
import com.mopub.common.OnNetworkInitializationFinishedListener
import com.mopub.common.Preconditions
import com.mopub.common.logging.MoPubLog
import com.mopub.mobileads.MoPubErrorCode

class HuaweiAdsAdapterConfiguration : BaseAdapterConfiguration() {
    override fun getAdapterVersion(): String {
        return ADAPTER_VERSION
    }

    override fun getBiddingToken(context: Context): String? {
        return null
    }

    override fun getMoPubNetworkName(): String {
        return MOPUB_NETWORK_NAME
    }

    override fun getNetworkSdkVersion(): String {
        val adapterVersion = adapterVersion
        return if (!TextUtils.isEmpty(adapterVersion)) adapterVersion.substring(
            0,
            adapterVersion.lastIndexOf('.')
        ) else ""
    }

    override fun initializeNetwork(
        context: Context,
        configuration: Map<String, String>?,
        listener: OnNetworkInitializationFinishedListener
    ) {
        Preconditions.checkNotNull(context)
        Preconditions.checkNotNull(listener)
        var networkInitializationSucceeded = false
        synchronized(HuaweiAdsAdapterConfiguration::class.java) {
            try {
                HwAds.init(context)
                networkInitializationSucceeded = true
            } catch (e: Exception) {
                MoPubLog.log(
                    MoPubLog.AdapterLogEvent.CUSTOM_WITH_THROWABLE, "Initializing AdMob has encountered " +
                            "an exception.", e
                )
            }
        }
        if (networkInitializationSucceeded) {
            listener.onNetworkInitializationFinished(
                HuaweiAdsAdapterConfiguration::class.java,
                MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS
            )
        } else {
            listener.onNetworkInitializationFinished(
                HuaweiAdsAdapterConfiguration::class.java,
                MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR
            )
        }
    }

    companion object {
        private const val ADAPTER_VERSION = "1.0"
        private const val MOPUB_NETWORK_NAME: String = "huaweiads"

    }
}
