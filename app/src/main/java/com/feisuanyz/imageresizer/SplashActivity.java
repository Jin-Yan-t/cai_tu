package com.feisuanyz.imageresizer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.feisuanyz.imageresizer.ads.AdConfig;
import com.feisuanyz.imageresizer.ads.AdManager;
import com.startapp.sdk.adsbase.StartAppAd;

public class SplashActivity extends AppCompatActivity {
    
    private static final int SPLASH_DELAY = 3000; // 3秒延迟
    private AdManager adManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // 初始化广告管理器
        adManager = AdManager.getInstance(this);
        adManager.initialize(AdConfig.START_IO_APP_ID, AdConfig.ENABLE_RETURN_AD);
        
        // 显示开屏广告
        showSplashAd();
    }
    
    private void showSplashAd() {
        // 显示开屏广告
        adManager.showSplashAd(this, new Runnable() {
            @Override
            public void run() {
                // 广告显示完成或超时，跳转到主界面
                navigateToMainActivity();
            }
        });
        
        // 设置超时跳转，防止广告加载失败时卡住
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                navigateToMainActivity();
            }
        }, SPLASH_DELAY);
    }
    
    private void navigateToMainActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // 关闭启动页
    }
    
    @Override
    public void onBackPressed() {
        // 禁止在启动页按返回键退出
    }
}