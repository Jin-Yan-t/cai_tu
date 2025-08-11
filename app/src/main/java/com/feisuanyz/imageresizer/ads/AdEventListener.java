package com.feisuanyz.imageresizer.ads;

public interface AdEventListener {
    void onAdLoaded(String adType);
    void onAdFailed(String adType, String error);
    void onAdClicked(String adType);
    void onAdClosed(String adType);
}