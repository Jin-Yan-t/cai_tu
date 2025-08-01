# 图片尺寸调整 - Android应用

这是一个基于Android的图像尺寸调整应用，使用Material Design设计，支持从相册选择图片、调整尺寸并保存。

## 功能特性

- 📸 从相册选择图片
- 📏 自定义调整图片宽度和高度
- 👀 实时预览调整效果
- 💾 保存调整后的图片到相册
- 🎨 Material Design界面设计

## 项目结构

```
看图-android/
├── app/
│   ├── src/main/
│   │   ├── java/com/feisuanyz/imageresizer/
│   │   │   └── MainActivity.java
│   │   └── res/
│   │       ├── layout/
│   │       │   └── activity_main.xml
│   │       ├── values/
│   │       │   ├── colors.xml
│   │       │   └── styles.xml
│   │       └── drawable/
│   │           └── image_border.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## 构建和运行

### 环境要求
- Android Studio Arctic Fox 或更高版本
- Java 8 或更高版本
- Android SDK 21 或更高版本

### 构建步骤

1. 打开Android Studio
2. 选择 "Open an existing Android Studio project"
3. 导航到 `d:\看图-android` 文件夹
4. 点击 "OK" 等待项目同步完成
5. 点击运行按钮 (▶️) 或按 Shift+F10

### 命令行构建

```bash
# Windows
gradlew.bat assembleDebug

# 生成的APK文件位置
app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

1. 打开应用，点击"选择图片"按钮
2. 从相册中选择要调整的图片
3. 在输入框中输入想要的宽度和高度
4. 点击"调整尺寸"按钮查看预览效果
5. 满意后点击"保存图片"按钮保存到相册

## 权限说明

应用需要以下权限：
- **读取外部存储**: 从相册选择图片
- **写入外部存储**: 保存调整后的图片

## 技术栈

- **语言**: Java
- **UI框架**: Android Material Design
- **构建工具**: Gradle
- **最低支持**: Android 5.0 (API 21)

## 注意事项

- 确保在Android 10及以上版本授予文件访问权限
- 大尺寸图片处理可能需要更多内存
- 建议在WiFi环境下使用以节省流量