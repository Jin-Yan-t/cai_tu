package com.feisuanyz.imageresizer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;



import java.io.IOException;


import java.io.IOException;
import java.io.InputStream;

import com.feisuanyz.imageresizer.ads.AdManager;
import com.feisuanyz.imageresizer.ads.AdConfig;
import com.feisuanyz.imageresizer.ads.AdManager.RewardedAdLoadListener;

public class MainActivity extends AppCompatActivity {
    
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    
    private ImageView imageView;
    private ImageView previewImageView;
    private TextView fileInfoText;
    private EditText widthInput;
    private EditText heightInput;
    private Button selectButton;
    private Button resizeButton;
    private Button saveButton;
    private Button rewardedAdButton;
    
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    private AdManager adManager;
    private int operationCount = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 初始化广告管理器
        adManager = AdManager.getInstance(this);
        adManager.initialize(AdConfig.START_IO_APP_ID, AdConfig.ENABLE_RETURN_AD);
        
        // 初始化广告
        initializeAds();
        
        // 初始化视图
        initViews();
        // 检查权限
        checkPermissions();
        // 设置点击事件
        setupClickListeners();
    }
    
    private void initializeAds() {
        // 加载横幅广告
        if (AdConfig.ENABLE_BANNER_AD) {
            adManager.loadBannerAd(this, AdConfig.BANNER_AD_CONTAINER_ID);
        }
        
        // 加载原生广告
        if (AdConfig.ENABLE_NATIVE_AD) {
            adManager.loadNativeAd(this, AdConfig.NATIVE_AD_CONTAINER_ID, null);
        }
        
        // 预加载激励广告
        if (AdConfig.ENABLE_REWARDED_AD) {
            adManager.loadRewardedAd(this, new AdManager.RewardedAdLoadListener() {
                @Override
                public void onRewardedAdLoaded() {
                    // 广告预加载成功
                }

                @Override
                        public void onRewardedAdFailed() {
                            // 广告预加载失败
                        }

                        @Override
                        public void onRewardedAdRewarded() {
                            // 广告完成观看
                        }

                @Override
                public void onRewardedAdClosed() {
                    // 广告关闭
                }
            });
        }
    }
    
    private void initViews() {
        imageView = findViewById(R.id.imageView);
        previewImageView = findViewById(R.id.previewImageView);
        fileInfoText = findViewById(R.id.fileInfoText);
        widthInput = findViewById(R.id.widthInput);
        heightInput = findViewById(R.id.heightInput);
        selectButton = findViewById(R.id.selectButton);
        resizeButton = findViewById(R.id.resizeButton);
        saveButton = findViewById(R.id.saveButton);
        rewardedAdButton = findViewById(R.id.rewardedAdButton);

        
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }
    }
    
    private void setupClickListeners() {
        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
                incrementOperationCount();
            }
        });
        resizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resizeImage();
                incrementOperationCount();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
                incrementOperationCount();
            }
        });
        
        rewardedAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AdConfig.ENABLE_REWARDED_AD) {
                    adManager.showRewardedAd(MainActivity.this, new AdManager.RewardedAdLoadListener() {
                        @Override
                        public void onRewardedAdLoaded() {
                            // 广告加载成功，自动显示
                        }

                        @Override
                        public void onRewardedAdFailed() {
                            Toast.makeText(MainActivity.this, "激励广告加载失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdRewarded() {
                            Toast.makeText(MainActivity.this, "恭喜获得奖励！现在可以免费保存图片", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onRewardedAdClosed() {
                            // 广告关闭，重新加载
                            adManager.loadRewardedAd(MainActivity.this, new RewardedAdLoadListener() {
                                @Override
                                public void onRewardedAdLoaded() {}
                                @Override
                                public void onRewardedAdFailed() {}
                                @Override
                                public void onRewardedAdRewarded() {}
                                @Override
                                public void onRewardedAdClosed() {}
                            });
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "激励广告功能已关闭", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    
    private void incrementOperationCount() {
        operationCount++;
        if (AdConfig.ENABLE_INTERSTITIAL_AD && operationCount % AdConfig.INTERSTITIAL_AD_INTERVAL == 0) {
            adManager.showInterstitialAd(this);
        }
    }
    
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                originalBitmap = BitmapFactory.decodeStream(inputStream);
                currentBitmap = originalBitmap;
                
                // 显示图片信息
                displayImageInfo(originalBitmap);
                
                // 自动填充宽高
                widthInput.setText(String.valueOf(originalBitmap.getWidth()));
                heightInput.setText(String.valueOf(originalBitmap.getHeight()));
                
                // 显示图片
                imageView.setImageBitmap(originalBitmap);
                
            } catch (IOException e) {
                Toast.makeText(this, "读取图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void displayImageInfo(Bitmap bitmap) {
        String info = String.format("文件名: 选择的图片\n尺寸: %d × %d 像素", 
                bitmap.getWidth(), bitmap.getHeight());
        fileInfoText.setText(info);
    }
    
    private void resizeImage() {
        if (originalBitmap == null) {
            Toast.makeText(this, "请先选择图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String widthStr = widthInput.getText().toString();
            String heightStr = heightInput.getText().toString();
            
            if (widthStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, "请输入宽度和高度", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int width = Integer.parseInt(widthStr);
            int height = Integer.parseInt(heightStr);
            
            if (width <= 0 || height <= 0) {
                Toast.makeText(this, "请输入有效的宽度和高度", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // 调整图片尺寸
            currentBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);
            
            // 显示调整后的图片
            previewImageView.setImageBitmap(currentBitmap);
            
            // 更新信息
            displayImageInfo(currentBitmap);
            
            Toast.makeText(this, "图片尺寸已调整", Toast.LENGTH_SHORT).show();

            
            
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void saveImage() {
        if (currentBitmap == null) {
            Toast.makeText(this, "没有可保存的图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            String fileName = "resized_image.jpg";
            MediaStore.Images.Media.insertImage(getContentResolver(), 
                    currentBitmap, fileName, "调整尺寸后的图片");
            
            Toast.makeText(this, "图片已保存到相册", Toast.LENGTH_SHORT).show();

            
            
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已获取", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }
    

    
    @Override
    protected void onResume() {
        super.onResume();
        if (adManager != null) {
            adManager.onResume(this);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (adManager != null) {
            adManager.onPause(this);
        }
    }
    
    @Override
    public void onBackPressed() {
        if (adManager != null) {
            adManager.onBackPressed(this);
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}