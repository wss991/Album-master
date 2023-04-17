package com.yanzhenjie.album.app.camera;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.yanzhenjie.album.mvp.BaseActivity;

/**
 * Created by Android Studio.
 * User: admin
 * Date: 2023/2/28
 * Time: 13:44
 */
public abstract class BaseRealCameraActivity extends BaseActivity {

    private String mCameraFilePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraFilePath = getIntent().getStringExtra(MediaStore.EXTRA_OUTPUT);
    }

    @Override
    public void finish() {
//        setResult(RESULT_OK);
        super.finish();
    }
}
