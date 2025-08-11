package com.feisuanyz.imageresizer;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * AdTest - 用于通过测试框架触发广告的测试类
 * 
 * 此类可以通过adb命令调用，用于在不打开UI的情况下触发广告
 * 使用方法：
 * adb shell am instrument -w -e class 'com.feisuanyz.imageresizer.AdTest#testShowRewardedAd' \
 *     com.feisuanyz.imageresizer/androidx.test.runner.AndroidJUnitRunner
 */
@RunWith(AndroidJUnit4.class)
public class AdTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    /**
     * 测试显示激励视频广告
     */
    @Test
    public void testShowRewardedAd() {
        // 获取MainActivity实例
        MainActivity activity = activityRule.getActivity();
        
        // 调用显示激励视频广告的方法
        activity.runOnUiThread(() -> {
            activity.showRewardedVideoAd();
        });
        
        // 等待广告显示和关闭
        try {
            Thread.sleep(30000); // 等待30秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 测试显示插屏广告
     */
    @Test
    public void testShowInterstitialAd() {
        // 获取MainActivity实例
        MainActivity activity = activityRule.getActivity();
        
        // 调用显示插屏广告的方法
        activity.runOnUiThread(() -> {
            activity.showInterstitialAd();
        });
        
        // 等待广告显示和关闭
        try {
            Thread.sleep(30000); // 等待30秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 测试随机显示广告
     */
    @Test
    public void testShowRandomAd() {
        // 获取MainActivity实例
        MainActivity activity = activityRule.getActivity();
        
        // 调用显示随机广告的方法
        activity.runOnUiThread(() -> {
            activity.showRandomAd();
        });
        
        // 等待广告显示和关闭
        try {
            Thread.sleep(30000); // 等待30秒
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}