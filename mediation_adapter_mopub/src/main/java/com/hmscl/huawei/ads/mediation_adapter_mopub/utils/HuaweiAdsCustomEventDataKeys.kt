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

class HuaweiAdsCustomEventDataKeys {
    companion object {
        const val KEY_EXTRA_APPLICATION_ID = "appid"
        const val AD_UNIT_ID_KEY = "adUnitID"
        const val TAG_FOR_CHILD_PROTECTION_KEY = "tagForChildProtection"
        const val TAG_FOR_UNDER_AGE_OF_PROMISE_KEY = "tagUnderAgeOfPromise"
        const val TAG_FOR_AD_CONTENT_CLASSIFICATION_KEY = "tagAdContentClassification"
        const val CONTENT_URL_KEY = "contentUrl"
        const val KEY_EXTRA_ORIENTATION_PREFERENCE = "orientation_preference"
        const val KEY_EXTRA_AD_CHOICES_PLACEMENT = "ad_choices_placement"
        const val KEY_EXPERIMENTAL_EXTRA_SWAP_MARGINS = "swap_margins"

        /* Custom Event Data - to be filled on MoPub Platform
        Banner, Interstitial, Rewarded
        {
            "appid":"111",
            "adUnitID": "222",
            "tagForChildProtection": "false",
            "tagUnderAgeOfPromise": "false",
            "tagAdContentClassification": "w/pi/j/a",
            "contentUrl" : "abc"
        }

        Native
        {
            "appid":"111",
            "adUnitID": "222",
            "tagForChildProtection": "false",
            "tagUnderAgeOfPromise": "false",
            "tagAdContentClassification": "w/pi/j/a",
            "contentUrl" : "abc",
            "orientation_preference": "aaa",
            "ad_choices_placement": "bbb",
            "swap_margins": "ccc"
        }
        */
    }
}