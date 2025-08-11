package com.feisuanyz.imageresizer.analytics;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WaterfallOptimizer {
    private static final String TAG = "WaterfallOptimizer";
    
    private static WaterfallOptimizer instance;
    private final AdRevenueTracker revenueTracker;
    private final Map<String, List<AdNetwork>> waterfallConfigs;
    private final Map<String, AdNetworkPerformance> networkPerformance;
    
    public static synchronized WaterfallOptimizer getInstance(Context context) {
        if (instance == null) {
            instance = new WaterfallOptimizer(context);
        }
        return instance;
    }
    
    private WaterfallOptimizer(Context context) {
        this.revenueTracker = AdRevenueTracker.getInstance(context);
        this.waterfallConfigs = new HashMap<>();
        this.networkPerformance = new HashMap<>();
        
        initializeWaterfallConfigs();
    }
    
    private void initializeWaterfallConfigs() {
        // 初始化不同广告位的瀑布流配置
        
        // Banner广告瀑布流
        List<AdNetwork> bannerWaterfall = new ArrayList<>();
        bannerWaterfall.add(new AdNetwork("startapp", 0.8, 95));
        bannerWaterfall.add(new AdNetwork("admob", 1.2, 90));
        bannerWaterfall.add(new AdNetwork("facebook", 1.0, 85));
        bannerWaterfall.add(new AdNetwork("unity", 0.7, 80));
        waterfallConfigs.put("banner", bannerWaterfall);
        
        // Interstitial广告瀑布流
        List<AdNetwork> interstitialWaterfall = new ArrayList<>();
        interstitialWaterfall.add(new AdNetwork("admob", 3.5, 90));
        interstitialWaterfall.add(new AdNetwork("facebook", 3.2, 88));
        interstitialWaterfall.add(new AdNetwork("startapp", 2.8, 85));
        interstitialWaterfall.add(new AdNetwork("unity", 2.5, 80));
        interstitialWaterfall.add(new AdNetwork("ironsource", 2.3, 75));
        waterfallConfigs.put("interstitial", interstitialWaterfall);
        
        // Rewarded广告瀑布流
        List<AdNetwork> rewardedWaterfall = new ArrayList<>();
        rewardedWaterfall.add(new AdNetwork("admob", 8.5, 92));
        rewardedWaterfall.add(new AdNetwork("facebook", 7.8, 90));
        rewardedWaterfall.add(new AdNetwork("unity", 7.2, 88));
        rewardedWaterfall.add(new AdNetwork("ironsource", 6.8, 85));
        rewardedWaterfall.add(new AdNetwork("applovin", 6.5, 83));
        waterfallConfigs.put("rewarded", rewardedWaterfall);
        
        // Native广告瀑布流
        List<AdNetwork> nativeWaterfall = new ArrayList<>();
        nativeWaterfall.add(new AdNetwork("facebook", 2.8, 88));
        nativeWaterfall.add(new AdNetwork("admob", 2.5, 85));
        nativeWaterfall.add(new AdNetwork("startapp", 2.0, 80));
        waterfallConfigs.put("native", nativeWaterfall);
    }
    
    public List<AdNetwork> getOptimizedWaterfall(String adType) {
        List<AdNetwork> waterfall = waterfallConfigs.get(adType);
        if (waterfall == null) {
            return new ArrayList<>();
        }
        
        // 根据实际表现数据重新排序
        List<AdNetwork> optimized = new ArrayList<>(waterfall);
        
        // 获取实际表现数据
        Map<String, AdRevenueTracker.AdPerformanceData> performance = revenueTracker.getAdPerformance();
        
        // 根据实际eCPM和填充率重新排序
        Collections.sort(optimized, new Comparator<AdNetwork>() {
            @Override
            public int compare(AdNetwork a, AdNetwork b) {
                double scoreA = calculateNetworkScore(a, adType, performance);
                double scoreB = calculateNetworkScore(b, adType, performance);
                return Double.compare(scoreB, scoreA); // 降序排序
            }
        });
        
        Log.d(TAG, "Optimized waterfall for " + adType + ": " + optimized);
        return optimized;
    }
    
    private double calculateNetworkScore(AdNetwork network, String adType, 
                                       Map<String, AdRevenueTracker.AdPerformanceData> performance) {
        String key = adType + "_" + network.getName();
        AdRevenueTracker.AdPerformanceData data = performance.get(key);
        
        if (data == null || data.getImpressions() < 100) {
            // 数据不足，使用预估数据
            return network.getEstimatedEcpm() * (network.getFillRate() / 100.0);
        }
        
        // 基于实际数据计算分数
        double actualEcpm = data.getECPM();
        double actualFillRate = data.getImpressions() > 0 ? 1.0 : 0.0; // 简化计算
        
        return actualEcpm * actualFillRate;
    }
    
    public void updateNetworkPerformance(String network, String adType, double revenue, boolean filled) {
        String key = network + "_" + adType;
        AdNetworkPerformance performance = networkPerformance.get(key);
        if (performance == null) {
            performance = new AdNetworkPerformance(network, adType);
            networkPerformance.put(key, performance);
        }
        
        if (filled) {
            performance.addFill(revenue);
        } else {
            performance.addNoFill();
        }
    }
    
    public WaterfallReport generateOptimizationReport() {
        WaterfallReport report = new WaterfallReport();
        
        for (Map.Entry<String, List<AdNetwork>> entry : waterfallConfigs.entrySet()) {
            String adType = entry.getKey();
            List<AdNetwork> waterfall = entry.getValue();
            
            WaterfallReport.AdTypeReport adTypeReport = new WaterfallReport.AdTypeReport(adType);
            
            for (AdNetwork network : waterfall) {
                String key = adType + "_" + network.getName();
                AdRevenueTracker.AdPerformanceData performance = 
                    revenueTracker.getAdPerformance().get(key);
                
                if (performance != null) {
                    WaterfallReport.NetworkPerformance networkPerf = 
                        new WaterfallReport.NetworkPerformance(
                            network.getName(),
                            performance.getImpressions(),
                            performance.getRevenue(),
                            performance.getECPM(),
                            performance.getCTR()
                        );
                    adTypeReport.addNetworkPerformance(networkPerf);
                }
            }
            
            report.addAdTypeReport(adTypeReport);
        }
        
        return report;
    }
    
    public static class AdNetwork {
        private final String name;
        private final double estimatedEcpm;
        private final double fillRate;
        
        public AdNetwork(String name, double estimatedEcpm, double fillRate) {
            this.name = name;
            this.estimatedEcpm = estimatedEcpm;
            this.fillRate = fillRate;
        }
        
        public String getName() { return name; }
        public double getEstimatedEcpm() { return estimatedEcpm; }
        public double getFillRate() { return fillRate; }
        
        @Override
        public String toString() {
            return name + "($" + estimatedEcpm + ")";
        }
    }
    
    public static class AdNetworkPerformance {
        private final String network;
        private final String adType;
        private int requests;
        private int fills;
        private double totalRevenue;
        
        public AdNetworkPerformance(String network, String adType) {
            this.network = network;
            this.adType = adType;
            this.requests = 0;
            this.fills = 0;
            this.totalRevenue = 0.0;
        }
        
        public void addFill(double revenue) {
            requests++;
            fills++;
            totalRevenue += revenue;
        }
        
        public void addNoFill() {
            requests++;
        }
        
        public double getFillRate() {
            return requests > 0 ? (double) fills / requests : 0;
        }
        
        public double getEcpm() {
            return fills > 0 ? (totalRevenue / fills) * 1000 : 0;
        }
    }
    
    public static class WaterfallReport {
        private final List<AdTypeReport> adTypeReports;
        
        public WaterfallReport() {
            this.adTypeReports = new ArrayList<>();
        }
        
        public void addAdTypeReport(AdTypeReport report) {
            adTypeReports.add(report);
        }
        
        public List<AdTypeReport> getAdTypeReports() {
            return adTypeReports;
        }
        
        public static class AdTypeReport {
            private final String adType;
            private final List<NetworkPerformance> networkPerformances;
            
            public AdTypeReport(String adType) {
                this.adType = adType;
                this.networkPerformances = new ArrayList<>();
            }
            
            public void addNetworkPerformance(NetworkPerformance performance) {
                networkPerformances.add(performance);
            }
            
            public String getAdType() { return adType; }
            public List<NetworkPerformance> getNetworkPerformances() { return networkPerformances; }
        }
        
        public static class NetworkPerformance {
            private final String network;
            private final int impressions;
            private final double revenue;
            private final double ecpm;
            private final double ctr;
            
            public NetworkPerformance(String network, int impressions, double revenue, double ecpm, double ctr) {
                this.network = network;
                this.impressions = impressions;
                this.revenue = revenue;
                this.ecpm = ecpm;
                this.ctr = ctr;
            }
            
            public String getNetwork() { return network; }
            public int getImpressions() { return impressions; }
            public double getRevenue() { return revenue; }
            public double getEcpm() { return ecpm; }
            public double getCtr() { return ctr; }
        }
    }
}