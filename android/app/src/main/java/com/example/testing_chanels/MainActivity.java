package com.example.testing_chanels;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;


public class MainActivity extends FlutterActivity {

    private static final String CHANNEL = "com.example.hbrecorder/screenRecording";
    private static final String ACTION_START_SCREEN_CAPTURE = "com.example.START_SCREEN_CAPTURE";
    private static final int SCREEN_RECORD_REQUEST_CODE = 1000;
    private static final int OVERLAY_PERMISSION_REQUEST_CODE = 1234;
    private static MediaProjectionManager mediaProjectionManager;
    private static MediaProjection mediaProjection;
    private static Handler handler;
    private static String outputPath;

        private void startScreenshotService() {
        Intent intent = new Intent(this, ScreenshotService.class);
        intent.putExtra("outputPath", outputPath);
        startScreenCapture();
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(
                        (call, result) -> {
                            if (call.method.equals("takeScreenshot")) {
                                outputPath = call.argument("outputPath");
                                startScreenCapture();
                                result.success(outputPath);
                            } else if (call.method.equals("startOverLay")) {
                                outputPath = call.argument("outputPath");

                                checkOverlayPermissionAndStartOverLay();
                                result.success(true);
                            } else {

                                result.notImplemented();
                            }
                        }
                );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter(ACTION_START_SCREEN_CAPTURE);
        registerReceiver(screenCaptureReceiver, filter);
    }


    private final BroadcastReceiver screenCaptureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_START_SCREEN_CAPTURE.equals(intent.getAction())) {
                startScreenCapture();
            }
        }
    };


    private void startScreenCapture() {
        Intent permissionIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(permissionIntent, SCREEN_RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCREEN_RECORD_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data);
                if (mediaProjection != null) {
                    captureScreenshot();
                }
            } else {
                Log.e("MainActivity", "User denied screen capture permission");
            }
        }
        else  if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                startService(new Intent(this, OverlayService.class));
            } else {
                Toast.makeText(this, "Overlay permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }


    void captureScreenshot() {

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        ImageReader imageReader = ImageReader.newInstance(metrics.widthPixels, metrics.heightPixels, PixelFormat.RGBA_8888, 2);
        mediaProjection.createVirtualDisplay(
                "Screenshot",
                metrics.widthPixels,
                metrics.heightPixels,
                metrics.densityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader.getSurface(),
                null,
                handler
        );

        // Capture screenshot and save to the provided path
        imageReader.setOnImageAvailableListener(reader -> {
            Image image = reader.acquireLatestImage();
            if (image != null) {
                Bitmap bitmap = imageToBitmap(image);
                saveBitmapToFile(bitmap, outputPath);
                image.close();
            }
        }, handler);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister broadcast receiver
        unregisterReceiver(screenCaptureReceiver);
    }





    private static Bitmap imageToBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer buffer = planes[0].getBuffer();
        int pixelStride = planes[0].getPixelStride();
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * image.getWidth();

        Bitmap bitmap = Bitmap.createBitmap(
                image.getWidth() + rowPadding / pixelStride,
                image.getHeight(),
                Bitmap.Config.ARGB_8888
        );
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    private static void saveBitmapToFile(Bitmap bitmap, String outputPath) {
        File screenshotFile = new File(outputPath, "screenshot.png");
        try (FileOutputStream fos = new FileOutputStream(screenshotFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Log.d("saveBitmapToFile", "Screenshot saved at: " + screenshotFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
        private void checkOverlayPermissionAndStartOverLay() {
        if (Settings.canDrawOverlays(this)) {
            Log.e("Permission", "Allowed for overlay");
            startService(new Intent(this, OverlayService.class));
        } else {
            Log.e("Permission", "Denied for overlay");
            requestOverlayPermission();
        }
    }

    private void requestOverlayPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
    }

}




