# Huawei-Mopub Mediation Github Documentation

![Latest Version](https://img.shields.io/badge/latestVersion-1.2.0-yellow) ![Kotlin](https://img.shields.io/badge/language-kotlin-blue)
<br>
![Supported Platforms](https://img.shields.io/badge/Supported_Platforms:-Native_Android_,_Unity_,_React_Native_,_Flutter-orange)

This is a project to demonstrate how to use MoPub’s mediation feature with Huawei Ads Kit.

# Compatibility

|   | Banner Ad | Interstitial Ad | Rewarded Ad | Native Ad |
| --- | --- | --- | --- | --- |
| Native (Java/Kotlin) | ✅ | ✅ | ✅ | ✅ |
| Unity |✅|✅| ✅ | ❌ |
| React Native | ❌ | ✅ | ❌ | ❌ |
| Flutter |✅|✅| ✅ | ❌ |


# How to start?
  
## Create an ad unit on Huawei Publisher Service

1. Sign in to [Huawei Developer Console](https://developer.huawei.com/consumer/en/console) and create an AdUnit

## Create a custom event on MoPub:

Make sure to check the article on **[How to use Huawei Ads with MoPub mediation](https://medium.com/huawei-developers/how-to-use-huawei-ads-with-mopub-mediation-1f49adfdb2b1)**

1. Sign in to your [Huawei Developer Console](https://developer.huawei.com/consumer/en/console) and create an AdUnit
2. Sign in to your [MoPub console](https://app.mopub.com/)
3. Go to one of your orders and create a new line item
4. Select **Network line item** as the type
5. Select **Custom SDK network** as the network
6. Enter the **Custom event class** and **Custom event data** according to the type of your Ad. Refer to the section below.

## Custom event class
| Ad Type        | Custom event class           |
| ------------- |:-------------:|
| Banner Ad      | com.hmscl.huawei.ads.mediation_adapter_mopub.banner |
| Interstitial Ad      | com.hmscl.huawei.ads.mediation_adapter_mopub.interstitial     |
| Rewarded Video Ad | com.hmscl.huawei.ads.mediation_adapter_mopub.rewarded     |
| Native Ad (Basic) | com.hmscl.huawei.ads.mediation_adapter_mopub.native_basic    |
| Native Ad (Advanced) | com.hmscl.huawei.ads.mediation_adapter_mopub.native_advanced    |

## Custom Event Parameters

**Note:** Using the event parameter keys while testing may result test ads to not return.

### Banner, Interstitial, Rewarded
```
{
    "adUnitID": "111",  <-- Mandatory Field
    "appid":"222",
    "tagForChildProtection": "false",
    "tagUnderAgeOfPromise": "false",
    "tagAdContentClassification": "false",
    "tagConsentString": "false",
}
```

### Native
```
{
    "adUnitID": "111",  <-- Mandatory Field
    "appid":"222",
    "tagForChildProtection": "false",
    "tagUnderAgeOfPromise": "false",
    "tagAdContentClassification": "false",
    "tagConsentString": "false",
    "orientation_preference" : "1"
    "ad_choices_placement" : "1"
    "swap_margins" : "1"
}
```
### Parameters Description
| Key | Description | Possible value |
| --- | --- | --- |
| adUnitID  | Huawei Ads Unit ID | String |
| appid | Huawei Ads App ID | String |
| tagForChildProtection | Sets the tag for child-directed content, to comply with the Children's Online Privacy Protection Act (COPPA). | <ul><li>`true`: You want your ad content to be COPPA-compliant (interest-based ads and remarketing ads will be disabled for the ad request). </li><li>`false`: You do not want your ad content to be COPPA-compliant. </li></ul> |
| tagUnderAgeOfPromise | Sets the tag for users in the European Economic Area (EEA) under the age of consent, to comply with the General Data Protection Regulation (GDPR). Ad requests with this tag enabled will be unable to obtain personalized ads. | <ul><li> `true`: You want the ad request to meet the ad standard for users under the age of consent. </li><li> `false`: You do not want the ad request to meet the ad standard for users under the age of consent. </li></ul>  |
| tagAdContentClassification | Sets the maximum ad content rating for the ad requests of your app. The ads obtained using this method have a content rating at or below the specified one. | <ul><li> `w`: content suitable for widespread audiences. </li><li> `pi`: content suitable for audiences under parental instructions. </li><li> `j`: content suitable for junior and older audiences. </li><li> `a`: content suitable only for adults. </li></ul> |
| tagConsentString | Sets the user consent string that complies with TCF 2.0. | String |
| orientation_preference (Native specific) | Sets the orientation of an ad image. | <ul><li> `0`: ANY </li><li> `1`: PORTRAIT </li><li> `2`: LANDSCAPE </li></ul> |
| ad_choices_placement (Native specific) | Sets the AdChoices icon position | <ul><li> `0`: TOP_LEFT </li><li> `1`: TOP_RIGHT </li><li> `2`: BOTTOM_RIGHT </li><li> `3`: BOTTOM_LEFT </li></ul> |
| swap_margins (Native specific) | Configure margin | <ul><li> `true`: Set margin on </li><li> `false`: Set margin off </li></ul> |

> Note: All values ​​must be String format.

Also all values can be set dynamically.

```
moPubNative.setLocalExtras(
    mapOf(
        TAG_FOR_CHILD_PROTECTION_KEY to true,
        TAG_FOR_UNDER_AGE_OF_PROMISE_KEY to true,
        TAG_FOR_AD_CONTENT_CLASSIFICATION_KEY to true,
        TAG_CONSENT_STRING to "TCF 2.0 String",
        KEY_EXTRA_ORIENTATION_PREFERENCE to 1,
        KEY_EXTRA_AD_CHOICES_PLACEMENT to 1,
        KEY_EXPERIMENTAL_EXTRA_SWAP_MARGINS to 1
    )
)
```
> Note: Be aware that custom events parameters in Mopub platform will override your code side changes on the parameters.

[Mediation Child Protection Document](https://github.com/Explore-In-HMS/huawei.ads.mopub_mediation/blob/main/Huawei_Mopub_Mediation_Child_Protection.pdf)

Make sure to check the article on [Huawei Mopub Mediation - Child Protection](https://medium.com/huawei-developers/huawei-mopub-mediation-child-protection-e34b4817269f)

<h1 id="integrate-huawei-sdk">
Integrate the Huawei Mediation SDK
</h1>

In the **project-level build.gradle**, include Huawei’s maven repository

```groovy
repositories {
    google()
    jcenter() // Also, make sure jcenter() is included
    maven { url 'https://developer.huawei.com/repo/' } // Add this line
    maven {url "https://jitpack.io"} // Add this line
}

...

allprojects {
    repositories {
        google()
        jcenter() // Also, make sure jcenter() is included
        maven { url 'https://developer.huawei.com/repo/' } //Add this line
        maven {url "https://jitpack.io"} // Add this line
    }
}
```
<h1 id="app-level">
</h1>

In the **app-level build.gradle**, include Huawei Ads dependency (required by the adapter) and the Huawei mediation adapter

```groovy
dependencies {
    implementation 'com.huawei.hms:ads:3.4.41.304'
    implementation 'com.github.Explore-In-HMS:huawei.ads.mopub_mediation:<latest_version>'
}
```
**Important:**: To add Huawei Ads Kit SDK and Mediation adapter to the cross platforms apps, the native project should be opened with Android Studio.

**Important:** A device with Huawei Mobile Services (HMS) installed is required.

## **Permissions**
The HUAWEI Ads SDK (com.huawei.hms:ads) has integrated the required permissions. Therefore, you do not need to apply for these permissions. <br />

**android.permission.ACCESS_NETWORK_STATE:** Checks whether the current network is available.   <br/>

**android.permission.ACCESS_WIFI_STATE:** Obtains the current Wi-Fi connection status and the information about WLAN hotspots. <br />

**android.permission.BLUETOOTH:** Obtains the statuses of paired Bluetooth devices. (The permission can be removed if not necessary.) <br />

**android.permission.CAMERA:** Displays AR ads in the Camera app. (The permission can be removed if not necessary.) <br />

**android.permission.READ_CALENDAR:** Reads calendar events and their subscription statuses. (The permission can be removed if not necessary.) <br />

**android.permission.WRITE_CALENDAR:** Creates a calendar event when a user clicks the subscription button in an ad. (The permission can be removed if not necessary.) <br />

# Platforms

## **Native**

This section demonstrates how to use MoPub mediation feature with Huawei Ads Kit on Native Android app. 

Firstly, integrate the MoPub SDK for Android:

[MoPub Android SDK](https://developers.mopub.com/publishers/android/integrate/) can be used for all ad types.

**Note:** Developers can find app level build.gradle in their project from __**"app-folder/app/build.gradle"**__

### **Banner Ads**

To use Banner ads in Native android apps, please check the MoPub SDK. Click [here](https://developers.mopub.com/publishers/android/banner/) to get more information about MoPub SDKs Banner Ad development.

### **Interstitial Ads**

To use Interstitial ads in Native android apps, please check the MoPub SDK. Click [here](https://developers.mopub.com/publishers/android/interstitial/) to get more information about MoPub SDKs Interstitial Ad development.

### **Rewarded Ads**

To use Rewarded ads in Native android apps, please check the MoPub SDK. Click [here](https://developers.mopub.com/publishers/android/rewarded-ad/) to get more information about MoPub SDKs Rewarded Ad development.

### **Native Ads**

To use Native ads in Native android apps, please check the MoPub SDK. Click [here](https://developers.mopub.com/publishers/android/native-recyclerview/) to get more information about MoPub SDKs Native Ad development.

## **Unity**

This section demonstrates how to use MoPub mediation feature with Huawei Ads Kit on Unity.

Make sure to check the article on [How to use Huawei Ads with Supported Ad Platforms in Unity ?](https://medium.com/huawei-developers/how-to-use-huawei-ads-with-supported-ad-platforms-in-unity-2be08c943a7f)

**Supported Ad Formats are:** Banner Ads, Interstitial Ads and Rewarded Ads.

Firstly, integrate the MoPub Unity SDK to Unity.

For more details on MoPub Unity SDK visit [here](https://developers.mopub.com/publishers/unity/integrate/)

### **Banner Ads**
To use Banner ads in Unity , please check the MoPub Unity SDK. Click [here](https://developers.mopub.com/publishers/unity/banner/) to get more information about Mopub Unity SDKs Banner Ad development. 

### **Interstitial Ads**
To use Interstitial ads in Unity, please check the MoPub Unity SDK. Click [here](https://developers.mopub.com/publishers/unity/interstitial/) to get more information about Mopub Unity SDKs Interstitial Ad development.

### **Rewarded Ads**
To use Rewarded ads in Unity, please check the MoPub Unity SDK. Click [here](https://developers.mopub.com/publishers/unity/rewarded-ad/) to get more information about Mopub Unity SDKs Banner Ad development.

#### **Step 1:** 
Make sure to switch to the Android Platform from **Build Settings -> Android -> Switch Platform**
#### **Step 2:**
**Edit -> Project Settings ->  Player -> Other Settings**<br>
In Other Settings set minimum API level to at least **21**.
#### **Step 3:**
**Edit -> Project Settings ->  Player -> Publishing Settings**<br>
In Publishing Settings select **“Custom Main Gradle Template”** , **“Custom Base Gradle Template”** and **“Custom Greadle Properties Template”** <br>
This will let you override **mainTemplate.gradle** , **baseProjectTemplate.gradle** and **gradleTemplate.properties** files in the project.
#### **Step 4:**
**baseProjectTemplate.gradle** is equal to **project-level gradle** so you have to include **Huawei's Maven repositories** from the Integrate the Huawei Mediation SDK section from [**here**](#integrate-huawei-sdk) <br>
**mainTemplate.gradle** is equal to **app-level build.gradle** so you have to include **dependencies** from the Integrate the Huawei Mediation SDK section from [**here**](#app-level).
#### **Step 5:**
Open **gradleTemplate.properties** and add the following lines
```groovy
android.useAndroidX=true
android.enableJetifier=true
```

**After these configurations is completed you can display Huawei Ads.**

**Note:** 
In case of any error on aaptOptions you can add the following line to aaptOptions in **launcherTemplate.gradle** which you override it by enabiling it from **Edit -> Project Settings ->  Player -> Publishing Settings**

```groovy
aaptOptions {
        noCompress = ['.ress', '.resource', '.obb'] + unityStreamingAssets.tokenize(', ')
        ignoreAssetsPattern = "!.svn:!.git:!.ds_store:!*.scc:.*:!CVS:!thumbs.db:!picasa.ini:!*~"
    }
```


## React Native

This section demonstrates how to use MoPub mediation feature with Huawei Ads Kit on React Native apps.

Make sure to check the article on [How to use Huawei Ads with MoPub mediation (React Native)](https://medium.com/huawei-developers/how-to-use-huawei-ads-with-mopub-mediation-react-native-7381cd339098)

**Important:** There is no official React Native SDK for MoPub, therefore third party SDKs has been used in the demonstrations.

Firstly, integrate the React Native MoPub SDK as below depending on type of ad:

For **Interstitial** ad [react-native-mopub-sdk](https://github.com/aliasad106/React-Native-Mopub-SDK) SDK can be used.

**Note:** Developers can find app level build.gradle in their project from __**"app-folder/android/app/build.gradle"**__

Then use the following sample codes based on specific ad types.
  
## Sample Codes Based on Ad Types

### **Banner Ads**

Banner ads are not supported with this SDK. To use banner ads in React Native app, please check the HMS Core Ads Kit React Native SDK. Click [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-Guides/banner-0000001050439147) to get more information about HMS Core React Native SDK.

### **Interstitial Ad**

```jsx
<TouchableOpacity style={{ width: 100, height: 30, backgroundColor: 'red', marginTop: 10 }} onPress={() =>
          RNMoPubInterstitial.loadAd()
        }>
          <Text>
            load ad
                    </Text>
        </TouchableOpacity>
```

### **Rewarded Ads**

Rewarded ads are not supported with this SDK. To use Rewarded ads in React Native app, please check the HMS Core Ads Kit React Native SDK. Click [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-Guides/reward-0000001050196920) to get more information about HMS Core React Native SDK.

### **Native Ads**

Native ads are not supported with this SDK. To use Native ads in React Native app, please check the HMS Core Ads Kit React Native SDK. Click [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-Guides/native-0000001050316236) to get more information about HMS Core React Native SDK.

## Flutter

This section demonstrates how to use MoPub mediation feature with Huawei Ads Kit on Flutter.

Make sure to check the article on [How to show Huawei ads in Flutter using Mopub mediation?)](https://medium.com/huawei-developers/how-to-show-huawei-ads-in-flutter-using-mopub-mediation-9e40adf8e45d)

**Important:** There is no official Flutter SDK for MoPub therefore third party SDKs has been used in the demonstrations.

Firstly, integrate the MoPub Flutter SDK as below depending on type of ad:

For **Banner** , **Interstitial** and **Rewarded** ad types [mopub\_flutter](https://pub.dev/packages/mopub_flutter/install) SDK can be used.

**Note:** Developers can find app level build.gradle in their project from __**"app-folder/android/app/build.gradle"**__

**Note:** To avoid **"java.lang.RuntimeException: Unable to get provider com.google.android.gms.ads.MobileAdsInitProvider"** error, an Admob ID needs to be added to the application. If both Admob and MoPub are not used in the project, add a sample Admob id to solve this exception.

**Solution:** Add this meta-data tag to the AndroidManifest.xml file (Open android side of the flutter project to edit Manifest file.)

```groovy
<meta-data
 	android:name="com.google.android.gms.ads.APPLICATION_ID"
 	android:value="ca-app-pub-3940256099942544~3347511713"/>
```

Then use the following sample codes based on specific ad types.

## Sample Codes Based on Ad Types

### **Banner Ad**
```dart
try {
   MoPub.init('ad_unit_id', testMode: true).then((_) {
_  loadRewardedAd();
_  loadInterstitialAd();
         });
 } 
```
```dart
MoPubBannerAd(
   adUnitId: 'ad_unit_id',
   bannerSize: BannerSize.STANDARD,
   keepAlive: true,
   listener: (result, dynamic) {
     print('$result');
         },
      );
```
### **Interstitial Ad**
```dart
void _loadInterstitialAd() {
  interstitialAd = MoPubInterstitialAd(
    'ad_unit_id',
        (result, args) {
      print('Interstitial $result');
    },
    reloadOnClosed: true,
  );
}
```
```dart
RaisedButton(
    onPressed: () async {
    interstitialAd.show();
    },
   child: Text('Show interstitial'),
 )
 ```
### **Rewarded Ads**
```dart
void _loadRewardedAd() {
  videoAd = MoPubRewardedVideoAd('ad_unit_id',
          (result, args) {
        setState(() {
          rewardedResult = '${result.toString()}____$args';
        });
        print('$result');
        if (result == RewardedVideoAdResult.GRANT_REWARD) {
          print('Grant reward: $args');
        }
      }, reloadOnClosed: true);
}
```
```dart
RaisedButton(
onPressed: () async {
   var result = await videoAd.isReady();
   print('Is Ready $result');
   if (result) {
    videoAd.show();
  }
},
child: Text('Show Video'),
)
```
### **Native Ads**

Native ads are not supported with this SDK. To use Native ads in Flutter app, please check the HMS Core Ads Kit Flutter SDK. Click [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-Guides/native-ads-0000001050198817) to get more information about HMS Core React Native SDK.

## Cordova

Because MoPub ads for Cordova platform is not officially supported and there is no stable MoPub plugin for Cordova, MoPub-Huawei Ads Mediation is not possible on Cordova platform.

Huawei Ads can still be used on Cordova, for implementation click [here](https://developer.huawei.com/consumer/en/doc/development/HMS-Plugin-Guides/introduction-0000001050437673).


# Screenshots

## MoPub Ads
<table>
<tr>
<td>
<img src="art/mopub_banner.png" width="200">

Banner Ad
</td>

<td>
<img src="art/mopub_interstitial.jpg" width="200">


Interstitial Ad
</td>

<td>
<img src="art/mopub_rewarded.png" width="200">

Rewarded Ad
</td>
<td>
<img src="art/mopub_native.png" width="200">

Native Ad
</td>
</tr>
</tr>
</table>

## Huawei Ads
<table>
<tr>
<td>
<img src="art/huawei_banner.jpg" width="200">

Banner Ad
</td>

<td>
<img src="art/huawei_interstitial.jpg" width="200">


Interstitial Ad
</td>

<td>
<img src="art/huawei_rewarded.jpg" width="200">

Rewarded Ad
</td>

<td>
<img src="art/huawei_native.jpg" width="200">

Native Ad
</td>

</tr>
</tr>
</table>



