package com.yanzhenjie.album.gpu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLException;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.FrameLayout;


import com.daasuu.gpuv.camerarecorder.CameraRecordListener;
import com.daasuu.gpuv.camerarecorder.GPUCameraRecorderBuilder;
import com.daasuu.gpuv.camerarecorder.LensFacing;
import com.yanzhenjie.album.R;
import com.yanzhenjie.album.mvp.BaseActivity;
import com.yanzhenjie.album.util.AlbumUtils;
import com.yanzhenjie.album.util.SystemBar;
import com.yanzhenjie.album.widget.CaptureView;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

public class CpuCameraActivity extends BaseActivity {

    private SampleCameraGLView sampleGLView;
    protected com.daasuu.gpuv.camerarecorder.GPUCameraRecorder GPUCameraRecorder;
    private String filepath;
    private CaptureView recordBtn;
    protected LensFacing lensFacing = LensFacing.BACK;
    protected int cameraWidth = 1280;
    protected int cameraHeight = 720;
    protected int videoWidth = 720;
    protected int videoHeight = 720;

    private boolean isPortrait = true;
    private boolean isVideo = false;
    private boolean isStartRecord = false;
    private String realPath;
    ;

    public static final String INSTANCE_CAMERA_IS_VIDEO = "INSTANCE_CAMERA_IS_VIDEO";
    public static final String INSTANCE_CAMERA_FILE_PATH = "INSTANCE_CAMERA_FILE_PATH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPortrait = GPUCamreaManager.getInstance().isPortrait();
        if (!isPortrait) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        SystemBar.setStatusBarColor(this, Color.TRANSPARENT);
        SystemBar.setNavigationBarColor(this, Color.TRANSPARENT);
        SystemBar.invasionNavigationBar(this);
        SystemBar.invasionNavigationBar(this);
        setContentView(R.layout.activity_cpu_camera);
        if (savedInstanceState != null) {
            isVideo = savedInstanceState.getBoolean(INSTANCE_CAMERA_IS_VIDEO);
            filepath = savedInstanceState.getString(INSTANCE_CAMERA_FILE_PATH);
        } else {
            Bundle bundle = getIntent().getExtras();
            assert bundle != null;
            isVideo = bundle.getBoolean(INSTANCE_CAMERA_IS_VIDEO);
            filepath = bundle.getString(INSTANCE_CAMERA_FILE_PATH);

        }
        recordBtn = findViewById(R.id.take);
        if (!isPortrait) {
            videoWidth = 720;
            videoHeight = 1280;
            cameraWidth = 720;
            cameraHeight = 1280;
        } else {
            videoWidth = 720;
            videoHeight = 1280;
            cameraWidth = 1280;
            cameraHeight = 720;
        }
        if (isVideo) {
//            recordBtn.setText("开始录制");
        } else {
//            recordBtn.setText("拍照");
        }
        findViewById(R.id.switch_flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GPUCameraRecorder.switchFlashMode();
            }
        });
        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isVideo) {
                    if (GPUCameraRecorder != null) {
                        if (isStartRecord) {
                            GPUCameraRecorder.stop();
//                            recordBtn.setText("开始录像");
                        } else {
                            try {
                                if (URLUtil.isContentUrl(filepath)) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        FileOutputStream fileOutputStream = (FileOutputStream) CpuCameraActivity.this.getContentResolver().openOutputStream(Uri.parse(filepath));
                                        GPUCameraRecorder.start(fileOutputStream.getFD());
                                    } else {
                                        realPath = AlbumUtils.getRealPath(CpuCameraActivity.this, Uri.parse(filepath));
                                        GPUCameraRecorder.start(realPath);
                                    }

                                } else {

                                    GPUCameraRecorder.start(filepath);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }


//                            recordBtn.setText("停止录像");
                        }
                        isStartRecord = !isStartRecord;
                    }
                } else {
//                    recordBtn.setText("拍照");
                    captureBitmap(new BitmapReadyCallbacks() {
                        @Override
                        public void onBitmapReady(final Bitmap bitmap) {
                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    saveAsPngImage(bitmap, filepath);

                                    setResult(RESULT_OK);
                                    finish();
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpCamera();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
    }

    private void releaseCamera() {
        if (sampleGLView != null) {
            sampleGLView.onPause();
        }

        if (GPUCameraRecorder != null) {
            GPUCameraRecorder.stop();
            GPUCameraRecorder.release();
            GPUCameraRecorder = null;
        }

        if (sampleGLView != null) {
            ((FrameLayout) findViewById(R.id.wrap_view)).removeView(sampleGLView);
            sampleGLView = null;
        }
    }


    private void setUpCameraView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FrameLayout frameLayout = findViewById(R.id.wrap_view);
                frameLayout.removeAllViews();
                sampleGLView = null;
                sampleGLView = new SampleCameraGLView(getApplicationContext());
                sampleGLView.setTouchListener(new SampleCameraGLView.TouchListener() {
                    @Override
                    public void onTouch(MotionEvent event, int width, int height) {
                        if (GPUCameraRecorder == null) return;
                        GPUCameraRecorder.changeManualFocusPoint(event.getX(), event.getY(), width, height);
                    }
                });
                frameLayout.addView(sampleGLView);

            }
        });

    }


    private void setUpCamera() {
        setUpCameraView();

        GPUCameraRecorder = new GPUCameraRecorderBuilder(this, sampleGLView)
                //.recordNoFilter(true)
                .cameraRecordListener(new CameraRecordListener() {
                    @Override
                    public void onGetFlashSupport(boolean flashSupport) {

                    }

                    @Override
                    public void onRecordComplete() {
                        setResult(RESULT_OK);
                        recordBtn.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                finish(); // 做一个延迟处理，不然刷新了以后还是没时间
                            }
                        }, 1000);

                    }

                    @Override
                    public void onRecordStart() {

                    }

                    @Override
                    public void onError(Exception exception) {
                        Log.e("GPUCameraRecorder", exception.toString());
                    }

                    @Override
                    public void onCameraThreadFinish() {

                    }

                    @Override
                    public void onVideoFileReady() {

                    }
                })
                .videoSize(videoWidth, videoHeight)
                .cameraSize(cameraWidth, cameraHeight)
                .lensFacing(lensFacing)
                .filter(GPUCamreaManager.getInstance().getGlFilter())
                .build();


    }

    private interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }

    private void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks) {
        GPUCameraRecorder.getGlPreviewRenderer().addDrawQueue(new Runnable() {
            @Override
            public void run() {
                EGL10 egl = (EGL10) EGLContext.getEGL();
                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
                final Bitmap snapshotBitmap = createBitmapFromGLSurface(sampleGLView.getMeasuredWidth(), sampleGLView.getMeasuredHeight(), gl);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
                    }
                });
            }
        });

    }

    private Bitmap createBitmapFromGLSurface(int w, int h, GL10 gl) {

        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2, texturePixel, blue, red, pixel;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    texturePixel = bitmapBuffer[offset1 + j];
                    blue = (texturePixel >> 16) & 0xff;
                    red = (texturePixel << 16) & 0x00ff0000;
                    pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            Log.e("CreateBitmap", "createBitmapFromGLSurface: " + e.getMessage(), e);
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public void saveAsPngImage(Bitmap bitmap, String filePath) {
        if (URLUtil.isContentUrl(filePath)) {
            Uri uri = Uri.parse(filePath);
            try {
                OutputStream outputStream = getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                File file = new File(filePath);
                FileOutputStream outStream = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                outStream.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            exportPngToGallery(CpuCameraActivity.this, filepath);
        }

    }


    private static void exportPngToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(INSTANCE_CAMERA_FILE_PATH, filepath);
        outState.putBoolean(INSTANCE_CAMERA_IS_VIDEO, isVideo);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();

    }
}