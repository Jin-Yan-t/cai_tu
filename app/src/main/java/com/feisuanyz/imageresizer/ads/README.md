# 广告模块重构文档

## 概述
本次重构将原本分散在MainActivity中的广告相关代码提取为独立的模块，实现了更好的代码组织和可维护性。

## 模块结构

### 核心类
- **AdManager**: 广告管理器，提供统一的广告API
- **AdConfig**: 广告配置类，集中管理广告参数
- **AdEventListener**: 广告事件监听接口
- **AdUtils**: 广告工具类，提供辅助功能

### 使用方式

#### 1. 初始化广告
```java
// 在Activity中初始化
AdManager adManager = AdManager.getInstance(context);
adManager.initialize(AdConfig.START_IO_APP_ID, AdConfig.ENABLE_RETURN_AD);
```

#### 2. 加载横幅广告
```java
adManager.loadBannerAd(activity, AdConfig.BANNER_AD_CONTAINER_ID);
```

#### 3. 加载原生广告
```java
adManager.loadNativeAd(activity, AdConfig.NATIVE_AD_CONTAINER_ID, 
    new AdManager.NativeAdLoadListener() {
        @Override
        public void onNativeAdLoaded() {
            // 广告加载成功
        }
        
        @Override
        public void onNativeAdFailed() {
            // 广告加载失败
        }
    });
```

#### 4. 显示插屏广告
```java
adManager.showInterstitialAd(activity);
```

## 配置说明

### AdConfig配置项
- `START_IO_APP_ID`: Start.io应用ID
- `ENABLE_BANNER_AD`: 是否启用横幅广告
- `ENABLE_INTERSTITIAL_AD`: 是否启用插屏广告
- `ENABLE_NATIVE_AD`: 是否启用原生广告
- `ENABLE_RETURN_AD`: 是否启用返回广告
- `INTERSTITIAL_AD_INTERVAL`: 插屏广告触发间隔

### 生命周期管理
确保在Activity的生命周期方法中调用对应的AdManager方法：

```java
@Override
protected void onResume() {
    super.onResume();
    adManager.onResume();
}

@Override
protected void onPause() {
    super.onPause();
    adManager.onPause();
}

@Override
public void onBackPressed() {
    adManager.onBackPressed();
}
```

## 优势
1. **单一职责**: 广告逻辑与业务逻辑分离
2. **可维护性**: 集中管理广告配置和逻辑
3. **可扩展性**: 易于添加新的广告类型
4. **可测试性**: 独立的广告模块便于单元测试
5. **复用性**: 可在其他Activity中复用AdManager