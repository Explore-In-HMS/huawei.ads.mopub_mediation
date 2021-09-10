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

import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.TAG_FOR_CHILD_PROTECTION_KEY
import com.hmscl.huawei.ads.mediation_adapter_mopub.utils.HuaweiAdsCustomEventDataKeys.Companion.TAG_FOR_UNDER_AGE_OF_PROMISE_KEY
import com.huawei.hms.ads.*

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
    val adContentClassification = extras[HuaweiAdsCustomEventDataKeys.TAG_FOR_AD_CONTENT_CLASSIFICATION_KEY]
    if (adContentClassification != null) {
        try {
            when (adContentClassification) {
                HuaweiContentClassificationKey.KEY_W -> requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_W)
                HuaweiContentClassificationKey.KEY_J -> requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_J)
                HuaweiContentClassificationKey.KEY_PI -> requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_PI)
                HuaweiContentClassificationKey.KEY_A -> requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_A)
                else -> requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_UNKOWN)
            }
        } catch (e: Exception) {
            requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_UNKOWN)
        }
    } else {
        requestConfigurationBuilder.setAdContentClassification(ContentClassification.AD_CONTENT_CLASSIFICATION_UNKOWN)
    }

    /**
     * Sets the type of ads to be requested. The HUAWEI Ads SDK supports two types of ads:
     * personalized and non-personalized.
     * The options are as follows:
     * allowAll: personalized and non-personalized ads.
     * allowNonPersonalized: non-personalized ads.
     */
    val nonPersonalizedAd = extras[HuaweiAdsCustomEventDataKeys.TAG_FOR_NON_PERSONALIZED_AD_KEY]
    if (nonPersonalizedAd != null) {
        try {
            when (nonPersonalizedAd) {
                HuaweiContentClassificationKey.KEY_0 -> requestConfigurationBuilder.setNonPersonalizedAd(NonPersonalizedAd.ALLOW_ALL)
                HuaweiContentClassificationKey.KEY_1 -> requestConfigurationBuilder.setNonPersonalizedAd(NonPersonalizedAd.ALLOW_NON_PERSONALIZED)
                else -> {
                    // No operation
                }
            }
        } catch (e: Exception) {
            // No operation
        }
    }

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

    return requestConfigurationBuilder
}