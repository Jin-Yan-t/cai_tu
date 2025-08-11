package com.feisuanyz.imageresizer.ads;

import com.feisuanyz.imageresizer.R;

public class AdConfig {
    // Start.io App ID
    public static final String START_IO_APP_ID = "207254439";
    
    // 广告开关配置
    public static final boolean ENABLE_BANNER_AD = true;
    public static final boolean ENABLE_INTERSTITIAL_AD = true;
    public static final boolean ENABLE_NATIVE_AD = true;
    public static final boolean ENABLE_RETURN_AD = true;
    public static final boolean ENABLE_REWARDED_AD = true;
    
    // 广告位ID配置
    public static final int BANNER_AD_CONTAINER_ID = R.id.bannerContainer;
    public static final int NATIVE_AD_CONTAINER_ID = R.id.nativeAdContainer;
    
    // 插屏广告触发条件
    public static final int INTERSTITIAL_AD_INTERVAL = 3; // 每3次操作显示一次
    
    // 原生广告配置
    public static final int NATIVE_AD_REFRESH_INTERVAL = 30000; // 30秒刷新一次
}