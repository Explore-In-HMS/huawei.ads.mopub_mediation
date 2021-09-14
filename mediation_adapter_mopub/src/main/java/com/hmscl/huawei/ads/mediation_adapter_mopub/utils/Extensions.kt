/*
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
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

import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.Constant.ContentClassification.KEY_A
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.Constant.ContentClassification.KEY_J
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.Constant.ContentClassification.KEY_PI
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.Constant.ContentClassification.KEY_W
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.Constant.PersonalAd.KEY_EXPLICIT_NO
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.Constant.PersonalAd.KEY_EXPLICIT_YES
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.TAG_CONSENT_STRING
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.TAG_FOR_CHILD_PROTECTION_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.TAG_FOR_UNDER_AGE_OF_PROMISE_KEY
import com.huawei.hms.ads.*
import com.huawei.hms.ads.nativead.NativeAd
import com.huawei.hms.ads.nativead.NativeAdConfiguration
import com.mopub.common.MoPub

fun prepareBuilderViaExtras(extras: Map<String, String>): RequestOptions.Builder {
    val requestConfigurationBuilder = HwAds.getRequestOptions().toBuilder()

    /**
     * Sets the maximum ad content rating for the ad requests of your app.
     * The ads obtained using this method have a content rating at or below the specified one.
     * The rating must be one of the following:
     * w: content suitable for widespread audiences.
     * pi: content suitable for audiences under parental instructions.
     * j: content suitable for junior and older audiences.
     * a: content suitable only for adults.
     */
    val adContentClassification =
            extras[HuaweiAdsCustomEventDataKeys.TAG_FOR_AD_CONTENT_CLASSIFICATION_KEY]
    if (adContentClassification != null) {
        try {
            when (adContentClassification) {
                KEY_W -> requestConfigurationBuilder.setAdContentClassification(
                        ContentClassification.AD_CONTENT_CLASSIFICATION_W
                )
                KEY_J -> requestConfigurationBuilder.setAdContentClassification(
                        ContentClassification.AD_CONTENT_CLASSIFICATION_J
                )
                KEY_PI -> requestConfigurationBuilder.setAdContentClassification(
                        ContentClassification.AD_CONTENT_CLASSIFICATION_PI
                )
                KEY_A -> requestConfigurationBuilder.setAdContentClassification(
                        ContentClassification.AD_CONTENT_CLASSIFICATION_A
                )
                else -> requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_UNKOWN)
            }
        } catch (e: Exception) {
            requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_UNKOWN)
        }
    } else {
        requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_UNKOWN)
    }

    /**
     * Sets the tag for child-directed content, to comply with the Children's Online Privacy Protection Act (COPPA).
     * The options are as follows:
     * protectionTrue: You want your ad content to be COPPA-compliant (interest-based ads and remarketing ads will be disabled for the ad request).
     * protectionFalse: You do not want your ad content to be COPPA-compliant.
     * protectionUnspecified: You have not specified whether your ad content needs to be COPPA-compliant.
     */
    val childProtection = extras[TAG_FOR_CHILD_PROTECTION_KEY]
    if (childProtection != null) {
        try {
            if (childProtection.toString().toBoolean()) {
                requestConfigurationBuilder.setTagForChildProtection(TagForChild.TAG_FOR_CHILD_PROTECTION_TRUE)
            } else {
                requestConfigurationBuilder.setTagForChildProtection(TagForChild.TAG_FOR_CHILD_PROTECTION_FALSE)
            }
        } catch (e: Exception) {
            requestConfigurationBuilder.setTagForChildProtection(TagForChild.TAG_FOR_CHILD_PROTECTION_UNSPECIFIED)
        }
    } else {
        requestConfigurationBuilder.setTagForChildProtection(TagForChild.TAG_FOR_CHILD_PROTECTION_UNSPECIFIED)
    }

    /**
     * Sets whether to process ad requests as directed to users under the age of consent.
     */
    // Publishers may want to mark their requests to receive treatment for users in the
    // European Economic Area (EEA) under the age of consent.
    val underAgeOfPromise = extras[TAG_FOR_UNDER_AGE_OF_PROMISE_KEY]
    if (underAgeOfPromise != null) {
        try {
            if (underAgeOfPromise.toString().toBoolean()) {
                requestConfigurationBuilder.setTagForUnderAgeOfPromise(UnderAge.PROMISE_TRUE)
            } else {
                requestConfigurationBuilder.setTagForUnderAgeOfPromise(UnderAge.PROMISE_FALSE)
            }
        } catch (e: Exception) {
            requestConfigurationBuilder.setTagForUnderAgeOfPromise(UnderAge.PROMISE_UNSPECIFIED)
        }
    } else {
        requestConfigurationBuilder.setTagForUnderAgeOfPromise(UnderAge.PROMISE_UNSPECIFIED)
    }

    /**
     * Sets whether to request only non-personalized ads.
     * ALLOW_ALL: personalized Requests both personalized and non-personalized ads (default).
     * ALLOW_NON_PERSONALIZED: Requests only non-personalized ads.
     *
     * https://developer.huawei.com/consumer/en/doc/development/HMSCore-References/nonpersonalizedad-0000001050064874#section5558312151218
     */
    try {
        val mPersonalInfoManager = MoPub.getPersonalInformationManager()
        when (mPersonalInfoManager?.personalInfoConsentStatus?.value) {
            KEY_EXPLICIT_YES -> {
                requestConfigurationBuilder.setNonPersonalizedAd(NonPersonalizedAd.ALLOW_ALL)
            }
            KEY_EXPLICIT_NO -> {
                requestConfigurationBuilder.setNonPersonalizedAd(NonPersonalizedAd.ALLOW_NON_PERSONALIZED)
            }
            else -> {
                requestConfigurationBuilder.setNonPersonalizedAd(NonPersonalizedAd.ALLOW_ALL)
            }

        }
    } catch (e: Exception) {
        requestConfigurationBuilder.setNonPersonalizedAd(NonPersonalizedAd.ALLOW_ALL)
    }

    val consent = extras[TAG_CONSENT_STRING]
    if (consent != null) {
        try {
            requestConfigurationBuilder.setConsent(consent)
        } catch (e: Exception) {
            //No operation.
        }
    }
    return requestConfigurationBuilder
}

/**
 *
 */
fun isValidOrientationExtra(extra: Any?): Boolean {
    return try {
        val value = extra.toString().toInt()
        value == NativeAdConfiguration.Direction.ANY || value == NativeAdConfiguration.Direction.LANDSCAPE || value == NativeAdConfiguration.Direction.PORTRAIT
    } catch (e: Exception) {
        false
    }
}

/**
 *
 */
fun isValidAdChoicesPlacementExtra(extra: Any?): Boolean {
    return try {
        val value = extra.toString().toInt()
        value == NativeAdConfiguration.ChoicesPosition.TOP_LEFT || value == NativeAdConfiguration.ChoicesPosition.TOP_RIGHT || value == NativeAdConfiguration.ChoicesPosition.BOTTOM_LEFT || value == NativeAdConfiguration.ChoicesPosition.BOTTOM_RIGHT
    } catch (e: Exception) {
        false
    }
}

/**
 *
 */
fun isValidHuaweiNativeAd(huaweiNativeAd: NativeAd): Boolean {
    return huaweiNativeAd.title != null && huaweiNativeAd.images != null && huaweiNativeAd.images.size > 0 && huaweiNativeAd.images[0] != null && huaweiNativeAd.callToAction != null
}
