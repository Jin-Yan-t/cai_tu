package com.feisuanyz.imageresizer.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdRevenueTracker {
    private static final String TAG = "AdRevenueTracker";
    private static final String PREFS_NAME = "AdRevenuePrefs";
    private static final String KEY_TOTAL_REVENUE = "total_revenue";
    private static final String KEY_IMPRESSIONS = "total_impressions";
    private static final String KEY_CLICKS = "total_clicks";
    
    private static AdRevenueTracker instance;
    private final SharedPreferences prefs;
    private final Map<String, AdPerformanceData> adPerformanceMap;
    private final Map<String, UserValueData> userValueMap;
    private final List<RevenueListener> listeners;
    
    // 实时数据
    private double currentSessionRevenue = 0.0;
    private int currentSessionImpressions = 0;
    private long sessionStartTime = 0;
    
    public interface RevenueListener {
        void onRevenueUpdate(double revenue, String adType, String placement);
        void onUserValueUpdate(String userId, double value);
    }
    
    public static synchronized AdRevenueTracker getInstance(Context context) {
        if (instance == null) {
            instance = new AdRevenueTracker(context.getApplicationContext());
        }
        return instance;
    }
    
    private AdRevenueTracker(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.adPerformanceMap = new ConcurrentHashMap<>();
        this.userValueMap = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.sessionStartTime = System.currentTimeMillis();
        
        // 初始化广告位数据
        initializeAdPlacements();
    }
    
    private void initializeAdPlacements() {
        String[] adTypes = {"banner", "interstitial", "rewarded", "native", "splash", "interactive"};
        String[] placements = {"main_screen", "resize_screen", "save_screen", "gallery_screen", "settings_screen"};
        
        for (String type : adTypes) {
            for (String placement : placements) {
                String key = type + "_" + placement;
                if (!adPerformanceMap.containsKey(key)) {
                    adPerformanceMap.put(key, new AdPerformanceData(type, placement));
                }
            }
        }
    }
    
    public void trackImpression(String adType, String placement, double revenue) {
        String key = adType + "_" + placement;
        AdPerformanceData data = adPerformanceMap.get(key);
        if (data != null) {
            data.addImpression(revenue);
            
            // 更新实时数据
            currentSessionRevenue += revenue;
            currentSessionImpressions++;
            
            // 保存到SharedPreferences
            saveRevenueData();
            
            // 通知监听器
            for (RevenueListener listener : listeners) {
                listener.onRevenueUpdate(revenue, adType, placement);
            }
            
            Log.d(TAG, String.format("Tracked: %s %s - $%.4f", adType, placement, revenue));
        }
    }
    
    public void trackClick(String adType, String placement) {
        String key = adType + "_" + placement;
        AdPerformanceData data = adPerformanceMap.get(key);
        if (data != null) {
            data.addClick();
            saveRevenueData();
        }
    }
    
    public void trackUserValue(String userId, double value) {
        UserValueData userData = userValueMap.get(userId);
        if (userData == null) {
            userData = new UserValueData(userId);
            userValueMap.put(userId, userData);
        }
        userData.addValue(value);
        
        for (RevenueListener listener : listeners) {
            listener.onUserValueUpdate(userId, value);
        }
    }
    
    public Map<String, AdPerformanceData> getAdPerformance() {
        return new HashMap<>(adPerformanceMap);
    }
    
    public List<UserValueData> getHighValueUsers(int limit) {
        List<UserValueData> users = new ArrayList<>(userValueMap.values());
        Collections.sort(users, new Comparator<UserValueData>() {
            @Override
            public int compare(UserValueData a, UserValueData b) {
                return Double.compare(b.getTotalValue(), a.getTotalValue());
            }
        });
        return users.subList(0, Math.min(limit, users.size()));
    }
    
    public double getTotalRevenue() {
        return prefs.getFloat(KEY_TOTAL_REVENUE, 0.0f);
    }
    
    public int getTotalImpressions() {
        return prefs.getInt(KEY_IMPRESSIONS, 0);
    }
    
    public int getTotalClicks() {
        return prefs.getInt(KEY_CLICKS, 0);
    }
    
    public double getCurrentSessionRevenue() {
        return currentSessionRevenue;
    }
    
    public int getCurrentSessionImpressions() {
        return currentSessionImpressions;
    }
    
    public double getSessionRPM() {
        if (currentSessionImpressions == 0) return 0.0;
        return (currentSessionRevenue / currentSessionImpressions) * 1000;
    }
    
    public void addRevenueListener(RevenueListener listener) {
        listeners.add(listener);
    }
    
    public void removeRevenueListener(RevenueListener listener) {
        listeners.remove(listener);
    }
    
    private void saveRevenueData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_TOTAL_REVENUE, (float) getTotalRevenue());
        editor.putInt(KEY_IMPRESSIONS, getTotalImpressions());
        editor.putInt(KEY_CLICKS, getTotalClicks());
        editor.apply();
    }
    
    public static class AdPerformanceData {
        private final String adType;
        private final String placement;
        private int impressions;
        private int clicks;
        private double revenue;
        private double ecpm;
        
        public AdPerformanceData(String adType, String placement) {
            this.adType = adType;
            this.placement = placement;
            this.impressions = 0;
            this.clicks = 0;
            this.revenue = 0.0;
            this.ecpm = 0.0;
        }
        
        public void addImpression(double revenue) {
            impressions++;
            this.revenue += revenue;
            calculateECPM();
        }
        
        public void addClick() {
            clicks++;
        }
        
        private void calculateECPM() {
            if (impressions > 0) {
                ecpm = (revenue / impressions) * 1000;
            }
        }
        
        public String getAdType() { return adType; }
        public String getPlacement() { return placement; }
        public int getImpressions() { return impressions; }
        public int getClicks() { return clicks; }
        public double getRevenue() { return revenue; }
        public double getECPM() { return ecpm; }
        public double getCTR() { return impressions > 0 ? (double) clicks / impressions : 0; }
    }
    
    public static class UserValueData {
        private final String userId;
        private double totalValue;
        private int impressionCount;
        private long firstSeen;
        private long lastSeen;
        
        public UserValueData(String userId) {
            this.userId = userId;
            this.totalValue = 0.0;
            this.impressionCount = 0;
            this.firstSeen = System.currentTimeMillis();
            this.lastSeen = System.currentTimeMillis();
        }
        
        public void addValue(double value) {
            totalValue += value;
            impressionCount++;
            lastSeen = System.currentTimeMillis();
        }
        
        public String getUserId() { return userId; }
        public double getTotalValue() { return totalValue; }
        public int getImpressionCount() { return impressionCount; }
        public double getAverageValue() { return impressionCount > 0 ? totalValue / impressionCount : 0; }
        public long getFirstSeen() { return firstSeen; }
        public long getLastSeen() { return lastSeen; }
    }
}