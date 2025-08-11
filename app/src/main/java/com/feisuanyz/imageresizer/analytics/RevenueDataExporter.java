package com.feisuanyz.imageresizer.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 收益数据导出器 - 用于将AdRevenueTracker的数据导出给Python脚本使用
 * 通过文件系统导出数据，避免使用广播接收器
 */
public class RevenueDataExporter {
    private static final String TAG = "RevenueDataExporter";
    private static final String REVENUE_DATA_FILE = "revenue_data.json";
    
    private final Context context;
    private final AdRevenueTracker revenueTracker;
    
    public RevenueDataExporter(Context context) {
        this.context = context;
        this.revenueTracker = AdRevenueTracker.getInstance(context);
        
        // 初始化时导出一次数据
        exportRevenueData();
        Log.d(TAG, "收益数据导出器已初始化");
    }
    
    /**
     * 注册广播接收器的方法已被移除
     * 此方法已被废弃，请使用 manualExportData() 方法代替
     */
    public void registerReceiver() {
        // 此方法已被废弃，不执行任何操作
        Log.d(TAG, "registerReceiver 方法已被废弃，请使用 manualExportData() 方法代替");
    }
    
    /**
     * 注销广播接收器的方法已被移除
     * 此方法已被废弃，请使用 manualExportData() 方法代替
     */
    public void unregisterReceiver() {
        // 此方法已被废弃，不执行任何操作
        Log.d(TAG, "unregisterReceiver 方法已被废弃，请使用 manualExportData() 方法代替");
    }
    
    /**
     * 手动导出收益数据
     * 可以在需要时调用此方法，例如在应用退出前或定期更新数据
     */
    public void manualExportData() {
        exportRevenueData();
    }
    
    private void exportRevenueData() {
        try {
            JSONObject data = new JSONObject();
            
            // 添加收益数据
            data.put("total_revenue", revenueTracker.getTotalRevenue());
            data.put("session_revenue", revenueTracker.getCurrentSessionRevenue());
            data.put("total_impressions", revenueTracker.getTotalImpressions());
            data.put("total_clicks", revenueTracker.getTotalClicks());
            data.put("session_impressions", revenueTracker.getCurrentSessionImpressions());
            data.put("session_rpm", revenueTracker.getSessionRPM());
            data.put("weekly_revenue", revenueTracker.getCurrentSessionRevenue() * 7); // 估算周收益
            data.put("timestamp", System.currentTimeMillis());
            
            // 写入文件
            writeToFile(data.toString());
            Log.d(TAG, "收益数据已导出: " + data.toString());
            
        } catch (JSONException e) {
            Log.e(TAG, "创建收益数据JSON失败", e);
        }
    }
    
    private void writeToFile(String data) {
        try {
            // 使用应用的私有存储空间
            File file = new File(context.getFilesDir(), REVENUE_DATA_FILE);
            FileWriter writer = new FileWriter(file);
            writer.write(data);
            writer.flush();
            writer.close();
            Log.d(TAG, "收益数据已写入文件: " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e(TAG, "写入收益数据文件失败", e);
        }
    }
    
    /**
     * 检查网络连接状态
     * @return 如果有网络连接返回true，否则返回false
     */
    private boolean isNetworkConnected() {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        } catch (Exception e) {
            Log.e(TAG, "检查网络连接状态失败", e);
        }
        return false;
    }
}