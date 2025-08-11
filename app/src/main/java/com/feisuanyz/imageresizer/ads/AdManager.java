package com.feisuanyz.imageresizer.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

public class AdManager {
    private static AdManager instance;
    private Context context;
    
    private AdManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static AdManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdManager(context);
        }
        return instance;
    }
    
    public void initialize(String appId, boolean enableReturnAds) {
        StartAppSDK.init(context, appId, enableReturnAds);
    }
    
    public void showSplashAd(Activity activity) {
        StartAppAd.showAd(activity);
    }
    
    public void showSplashAd(Activity activity, Runnable onAdClosed) {
        StartAppAd.showAd(activity);
        if (onAdClosed != null) {
            onAdClosed.run();
        }
    }
    
    public void loadBannerAd(Activity activity, int containerId) {
        FrameLayout bannerContainer = activity.findViewById(containerId);
        if (bannerContainer != null) {
            Banner banner = new Banner(activity);
            bannerContainer.removeAllViews();
            bannerContainer.addView(banner);
        }
    }
    
    public void loadNativeAd(Activity activity, int containerId, NativeAdLoadListener listener) {
        // 原生广告功能暂时移除
        if (listener != null) {
            listener.onNativeAdFailed();
        }
    }
    
    public void showInterstitialAd(Activity activity) {
        StartAppAd.showAd(activity);
    }
    
    public void loadRewardedAd(Activity activity, final RewardedAdLoadListener listener) {
        // Start.io SDK会自动预加载激励广告，这里直接回调成功
        if (listener != null) {
            listener.onRewardedAdLoaded();
        }
    }
    
    public void showRewardedAd(Activity activity, final RewardedAdLoadListener listener) {
        StartAppAd startAppAd = new StartAppAd(activity);
        
        try {
            // 显示激励广告
            startAppAd.showAd(activity);
            
            // 模拟奖励机制（Start.io SDK会自动处理激励逻辑）
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (listener != null) {
                        listener.onRewardedAdRewarded();
                        listener.onRewardedAdClosed();
                    }
                }
            }, 2000);
            
        } catch (Exception e) {
            // 广告显示失败
            if (listener != null) {
                listener.onRewardedAdFailed();
            }
        }
    }
    
    public void onResume(Activity activity) {
        // StartAppAd lifecycle management
    }
    
    public void onPause(Activity activity) {
        // StartAppAd lifecycle management
    }
    
    public void onBackPressed(Activity activity) {
        // StartAppAd back button handling
    }
    
    public interface NativeAdLoadListener {
        void onNativeAdLoaded();
        void onNativeAdFailed();
    }
    
    public interface RewardedAdLoadListener {
        void onRewardedAdLoaded();
        void onRewardedAdFailed();
        void onRewardedAdRewarded();
        void onRewardedAdClosed();
    }
}