package com.feisuanyz.imageresizer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;

import java.io.IOException;
import java.io.InputStream;

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
    
    private Bitmap originalBitmap;
    private Bitmap currentBitmap;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 初始化Start.io SDK
        StartAppSDK.init(this, "206466271", true);
        
        setContentView(R.layout.activity_main);
        
        // 初始化视图
        initViews();
        
        // 检查权限
        checkPermissions();
        
        // 设置点击事件
        setupClickListeners();
        
        // 加载横幅广告
        loadBannerAd();
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
        selectButton.setOnClickListener(v -> openImagePicker());
        resizeButton.setOnClickListener(v -> resizeImage());
        saveButton.setOnClickListener(v -> saveImage());
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
    
    private void loadBannerAd() {
        // 横幅广告会在布局中自动加载
    }
    
    private void loadInterstitialAd() {
        final StartAppAd interstitialAd = new StartAppAd(this);
        interstitialAd.loadAd();
    }
    
    private void showInterstitialAd() {
        StartAppAd.showAd(this);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // 显示插屏广告
        showInterstitialAd();
    }
}