/*
 * Copyright 2016 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.album.util;

import static com.yanzhenjie.album.gpu.CpuCameraActivity.INSTANCE_CAMERA_FILE_PATH;
import static com.yanzhenjie.album.gpu.CpuCameraActivity.INSTANCE_CAMERA_IS_VIDEO;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;


import com.yanzhenjie.album.gpu.CpuCameraActivity;
import com.yanzhenjie.album.provider.CameraFileProvider;
import com.yanzhenjie.album.widget.divider.Api20ItemDivider;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;
import com.yanzhenjie.album.widget.divider.Divider;

import java.io.File;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * <p>Helper for album.</p>
 * Created by Yan Zhenjie on 2016/10/30.
 */
public class AlbumUtils {

    private static final String CACHE_DIRECTORY = "AlbumCache";

    /**
     * Get a writable root directory.
     *
     * @param context context.
     * @return {@link File}.
     */
    @NonNull
    public static File getAlbumRootPath(Context context) {
        if (sdCardIsAvailable()) {
            return new File(Environment.getExternalStorageDirectory(), CACHE_DIRECTORY);
        } else {
            return new File(context.getFilesDir(), CACHE_DIRECTORY);
        }
    }

    /**
     * SD card is available.
     *
     * @return true when available, other wise is false.
     */
    public static boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().canWrite();
        } else
            return false;
    }

    /**
     * Setting {@link Locale} for {@link Context}.
     *
     * @param context to set the specified locale context.
     * @param locale  locale.
     */
    @NonNull
    public static Context applyLanguageForContext(@NonNull Context context, @NonNull Locale locale) {
        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            resources.updateConfiguration(config, resources.getDisplayMetrics());
            return context;
        }
    }

    /**
     * Take picture.
     *
     * @param activity    activity.
     * @param requestCode code, see {@link Activity#onActivityResult(int, int, Intent)}.
     * @param outPath     file path.
     */
    public static void takeImage(@NonNull Activity activity, int requestCode, String outPath) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri uri = getUri(activity, outPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Take video.
     *
     * @param activity    activity.
     * @param requestCode code, see {@link Activity#onActivityResult(int, int, Intent)}.
     * @param outPath     file path.
     * @param quality     currently value 0 means low quality, suitable for MMS messages, and  value 1 means high quality.
     * @param duration    specify the maximum allowed recording duration in seconds.
     * @param limitBytes  specify the maximum allowed size.
     */
    public static void takeVideo(@NonNull Activity activity, int requestCode, String outPath,
                                 @IntRange(from = 0, to = 1) int quality,
                                 @IntRange(from = 1) long duration,
                                 @IntRange(from = 1) long limitBytes) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        Uri uri = getUri(activity, outPath);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, quality);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, duration);
        intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, limitBytes);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Generates an externally accessed URI based on path.
     *
     * @param context context.
     * @param outPath file path.
     * @return the uri address of the file.
     */
    @NonNull
    public static Uri getUri(@NonNull Context context, @NonNull String outPath) {
        Uri uri = null;
        if (URLUtil.isContentUrl(outPath)) {
            uri = Uri.parse(outPath);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, CameraFileProvider.getProviderName(context), new File(outPath));

            } else {
                uri = Uri.fromFile(new File(outPath));
            }
        }
        return uri;
    }

    /**
     * Generate a random jpg file path.
     *
     * @return file path.
     * @deprecated use {@link #randomJPGPath(Context)} instead.
     * <p>
     * Environment.getExternalStorageDirectory()
     * 拍照不成功，需要用FileProvider
     * File bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
     * Uri.parse() 可以拍照成功
     */
    @NonNull
    @Deprecated
    public static String randomJPGPath() {
        File bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        return randomJPGPath(bucket);
    }

    /**
     * Generate a random jpg file path.
     *
     * @param context context.
     * @return file path.
     */
    @NonNull
    public static String randomJPGPath(Context context) {
        String filePath;
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            filePath = randomJPGPath(context.getCacheDir());
        } else {
            filePath = AlbumUtils.randomJPGPath(context);
        }
        return filePath;
    }

    /**
     * Generates a random jpg file path in the specified directory.
     *
     * @param bucket specify the directory.
     * @return file path.
     */
    @NonNull
    public static String randomJPGPath(File bucket) {
        return randomMediaPath(bucket, ".jpg");
    }

    /**
     * Generate a random mp4 file path.
     *
     * @return file path.
     * @deprecated use {@link #randomMP4Path(Context)} instead.
     */
    @NonNull
    @Deprecated
    public static String randomMP4Path() {
        File bucket = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        return randomMP4Path(bucket);
    }

    /**
     * Generate a random mp4 file path.
     *
     * @param context context.
     * @return file path.
     */
    @NonNull
    public static String randomMP4Path(Context context) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return randomMP4Path(context.getCacheDir());
        }
        return randomMP4Path();
    }

    /**
     * Generates a random mp4 file path in the specified directory.
     *
     * @return file path.
     */
    @NonNull
    public static String randomMP4Path(File bucket) {
        return randomMediaPath(bucket, ".mp4");
    }

    /**
     * Generates a random file path using the specified suffix name in the specified directory.
     *
     * @param bucket    specify the directory.
     * @param extension extension.
     * @return file path.
     */
    @NonNull
    private static String randomMediaPath(File bucket, String extension) {
        if (bucket.exists() && bucket.isFile()) bucket.delete();
        if (!bucket.exists()) bucket.mkdirs();
        String outFilePath = AlbumUtils.getNowDateTime("yyyyMMdd_HHmmssSSS") + "_" + getMD5ForString(UUID.randomUUID().toString()) + extension;
        File file = new File(bucket, outFilePath);
        return file.getAbsolutePath();
    }

    /**
     * Format the current time in the specified format.
     *
     * @return the time string.
     */
    @NonNull
    public static String getNowDateTime(@NonNull String format) {
        SimpleDateFormat formatter = new SimpleDateFormat(format, Locale.ENGLISH);
        Date curDate = new Date(System.currentTimeMillis());
        return formatter.format(curDate);
    }

    /**
     * Get the mime type of the file in the url.
     *
     * @param url file url.
     * @return mime type.
     */
    public static String getMimeType(String url) {
        String extension = getExtension(url);
        if (!MimeTypeMap.getSingleton().hasExtension(extension)) return "";

        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return TextUtils.isEmpty(mimeType) ? "" : mimeType;
    }

    /**
     * Get the file extension in url.
     *
     * @param url file url.
     * @return extension.
     */
    public static String getExtension(String url) {
        url = TextUtils.isEmpty(url) ? "" : url.toLowerCase();
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        return TextUtils.isEmpty(extension) ? "" : extension;
    }

    /**
     * Specifies a tint for {@code drawable}.
     *
     * @param drawable drawable target, mutate.
     * @param color    color.
     */
    public static void setDrawableTint(@NonNull Drawable drawable, @ColorInt int color) {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable.mutate()), color);
    }

    /**
     * Specifies a tint for {@code drawable}.
     *
     * @param drawable drawable target, mutate.
     * @param color    color.
     * @return convert drawable.
     */
    @NonNull
    public static Drawable getTintDrawable(@NonNull Drawable drawable, @ColorInt int color) {
        drawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTint(drawable, color);
        return drawable;
    }

    /**
     * {@link ColorStateList}.
     *
     * @param normal    normal color.
     * @param highLight highLight color.
     * @return {@link ColorStateList}.
     */
    public static ColorStateList getColorStateList(@ColorInt int normal, @ColorInt int highLight) {
        int[][] states = new int[6][];
        states[0] = new int[]{android.R.attr.state_checked};
        states[1] = new int[]{android.R.attr.state_pressed};
        states[2] = new int[]{android.R.attr.state_selected};
        states[3] = new int[]{};
        states[4] = new int[]{};
        states[5] = new int[]{};
        int[] colors = new int[]{highLight, highLight, highLight, normal, normal, normal};
        return new ColorStateList(states, colors);
    }

    /**
     * Change part of the color of CharSequence.
     *
     * @param content content text.
     * @param start   start index.
     * @param end     end index.
     * @param color   color.
     * @return {@code SpannableString}.
     */
    @NonNull
    public static SpannableString getColorText(@NonNull CharSequence content, int start, int end, @ColorInt int color) {
        SpannableString stringSpan = new SpannableString(content);
        stringSpan.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return stringSpan;
    }

    /**
     * Return a color-int from alpha, red, green, blue components.
     *
     * @param color color.
     * @param alpha alpha, alpha component [0..255] of the color.
     */
    @ColorInt
    public static int getAlphaColor(@ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /**
     * Generate divider.
     *
     * @param color color.
     * @return {@link Divider}.
     */
    public static Divider getDivider(@ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new Api21ItemDivider(color);
        }
        return new Api20ItemDivider(color);
    }

    /**
     * Time conversion.
     *
     * @param duration ms.
     * @return such as: {@code 00:00:00}, {@code 00:00}.
     */
    @NonNull
    public static String convertDuration(@IntRange(from = 1) long duration) {
        duration /= 1000;
        int hour = (int) (duration / 3600);
        int minute = (int) ((duration - hour * 3600) / 60);
        int second = (int) (duration - hour * 3600 - minute * 60);

        String hourValue = "";
        String minuteValue;
        String secondValue;
        if (hour > 0) {
            if (hour >= 10) {
                hourValue = Integer.toString(hour);
            } else {
                hourValue = "0" + hour;
            }
            hourValue += ":";
        }
        if (minute > 0) {
            if (minute >= 10) {
                minuteValue = Integer.toString(minute);
            } else {
                minuteValue = "0" + minute;
            }
        } else {
            minuteValue = "00";
        }
        minuteValue += ":";
        if (second > 0) {
            if (second >= 10) {
                secondValue = Integer.toString(second);
            } else {
                secondValue = "0" + second;
            }
        } else {
            secondValue = "00";
        }
        return hourValue + minuteValue + secondValue;
    }

    /**
     * Get the MD5 value of string.
     *
     * @param content the target string.
     * @return the MD5 value.
     */
    public static String getMD5ForString(String content) {
        StringBuilder md5Buffer = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] tempBytes = digest.digest(content.getBytes());
            int digital;
            for (int i = 0; i < tempBytes.length; i++) {
                digital = tempBytes[i];
                if (digital < 0) {
                    digital += 256;
                }
                if (digital < 16) {
                    md5Buffer.append("0");
                }
                md5Buffer.append(Integer.toHexString(digital));
            }
        } catch (Exception ignored) {
            return Integer.toString(content.hashCode());
        }
        return md5Buffer.toString();
    }

    public static void takeGPUImage(@NonNull Activity activity,
                                    int requestCode,
                                    String outPath) {
        Intent intent = new Intent(activity, CpuCameraActivity.class);
        intent.putExtra(INSTANCE_CAMERA_FILE_PATH, outPath);
        intent.putExtra(INSTANCE_CAMERA_IS_VIDEO, false);
        activity.startActivityForResult(intent, requestCode);

    }


    public static void takeGPUVideo(@NonNull Activity activity,
                                    int requestCode,
                                    String outPath) {
        Intent intent = new Intent(activity, CpuCameraActivity.class);
        intent.putExtra(INSTANCE_CAMERA_FILE_PATH, outPath);
        intent.putExtra(INSTANCE_CAMERA_IS_VIDEO, true);
        activity.startActivityForResult(intent, requestCode);

    }


    /**
     * @param context
     * @param parent  api>=29 以上传入的 类似 Environment.DIRECTORY_PICTURES 或者 xx/xx 在那个文件下创建该文件
     *                api<29 以上传入的 传入的是完整路径 story/xx/xx
     *                相对路径
     *                注意：不能有parent处理
     * @return
     */
    public static String newTakePhotoPath(Context context) {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        ContentValues contentValues = new ContentValues();
        String fileName = "IMG_" + getNowDateTime("yyyyMMddHHmmssSSS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.ImageColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
        }
        contentValues.put(MediaStore.Images.ImageColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        Uri uri;
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            uri = context.getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, contentValues);
        }
        if (uri != null) {
            return uri.toString();
        }
        return null;
    }

    public static String newTakeVideoPath(Context context) {
        String status = Environment.getExternalStorageState();
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        ContentValues contentValues = new ContentValues();
        String fileName = "VD_" + getNowDateTime("yyyyMMddHHmmssSSS");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Video.VideoColumns.RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
        }
        contentValues.put(MediaStore.Video.VideoColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.Images.ImageColumns.MIME_TYPE, "video/MP4");
        Uri uri;
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            uri = context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            uri = context.getContentResolver().insert(MediaStore.Video.Media.INTERNAL_CONTENT_URI, contentValues);
        }
        if (uri != null) {
            return uri.toString();
        }
        return null;
    }

    public static String getRealPath(Context context, Uri uri) {
        long id = ContentUris.parseId(uri);
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA};
        String selection = MediaStore.Files.FileColumns._ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(id)};
        Cursor cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external")
                , projection, selection, selectionArgs, null);
        if (cursor != null && cursor.getColumnCount() > 0) {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(column_index);
            return path;
        }
        return uri.toString();
    }
}