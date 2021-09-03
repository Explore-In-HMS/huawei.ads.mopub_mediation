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

import java.util.*

class HuaweiAdsViewBinder private constructor (builder: Builder) {
    class Builder(val layoutId: Int) {
        var mediaLayoutId = 0
        var titleId = 0
        var textId = 0
        var iconImageId = 0
        var callToActionId = 0
        var privacyInformationIconImageId = 0
        var sponsoredTextId = 0
        var extras: MutableMap<String, Int>
        fun mediaLayoutId(mediaLayoutId: Int): Builder {
            this.mediaLayoutId = mediaLayoutId
            return this
        }

        fun titleId(titleId: Int): Builder {
            this.titleId = titleId
            return this
        }

        fun textId(textId: Int): Builder {
            this.textId = textId
            return this
        }

        fun iconImageId(iconImageId: Int): Builder {
            this.iconImageId = iconImageId
            return this
        }

        fun callToActionId(callToActionId: Int): Builder {
            this.callToActionId = callToActionId
            return this
        }

        fun privacyInformationIconImageId(privacyInformationIconImageId: Int): Builder {
            this.privacyInformationIconImageId = privacyInformationIconImageId
            return this
        }

        fun sponsoredTextId(sponsoredTextId: Int): Builder {
            this.sponsoredTextId = sponsoredTextId
            return this
        }

        fun addExtras(resourceIds: Map<String, Int>?): Builder {
            extras = HashMap(resourceIds)
            return this
        }

        fun addExtra(key: String, resourceId: Int): Builder {
            extras[key] = resourceId
            return this
        }

        fun build() : HuaweiAdsViewBinder {
            return HuaweiAdsViewBinder(this)
        }

        init {
            extras = HashMap()
        }
    }

    var layoutId = 0
    var mediaLayoutId = 0
    var titleId = 0
    var textId = 0
    var callToActionId = 0
    var iconImageId = 0
    var privacyInformationIconImageId = 0
    var sponsoredTextId = 0
    var extras: Map<String, Int>? = null

    init {
        layoutId = builder.layoutId
        mediaLayoutId = builder.mediaLayoutId
        titleId = builder.titleId
        textId = builder.textId
        callToActionId = builder.callToActionId
        iconImageId = builder.iconImageId
        privacyInformationIconImageId = builder.privacyInformationIconImageId
        sponsoredTextId = builder.sponsoredTextId
        extras = builder.extras
    }
}