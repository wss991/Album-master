package com.yanzhenjie.album.sample.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.yanzhenjie.album.app.camera.BaseRealCameraActivity;
import com.yanzhenjie.album.sample.R;

public class RealCameraActivity extends BaseRealCameraActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_camera);
    }
}