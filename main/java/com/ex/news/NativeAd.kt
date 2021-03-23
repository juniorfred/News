package com.ex.news

import com.google.android.gms.ads.formats.UnifiedNativeAd

data class NativeAd(val ad: UnifiedNativeAd) : StoriesAdapterItem(){

    override val itemType: Int get() = NATIVE_ADS_TYPE
}