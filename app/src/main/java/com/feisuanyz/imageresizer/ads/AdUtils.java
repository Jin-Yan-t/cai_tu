package com.feisuanyz.imageresizer.ads;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class AdUtils {
    
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = 
            (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    
    public static void showNetworkError(Context context) {
        Toast.makeText(context, "网络连接不可用，请检查网络设置", Toast.LENGTH_SHORT).show();
    }
    
    public static void logAdEvent(String event, String adType) {
        android.util.Log.d("AdManager", "Event: " + event + " - Type: " + adType);
    }
    
    public static String getAdTypeString(int adType) {
        switch (adType) {
            case 0: return "Banner";
            case 1: return "Interstitial";
            case 2: return "Native";
            case 3: return "Rewarded";
            default: return "Unknown";
        }
    }
}