package com.example.testing_chanels;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.view.PreviewView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class OverlayService extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private boolean isOverlayVisible = false;
    private PreviewView previewView;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        overlayView = inflater.inflate(R.layout.overlay_view, null);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

        overlayView.setOnClickListener(v -> {
            Log.d("OverlayService", "Overlay clicked");

            Intent intent = new Intent("com.example.START_SCREEN_CAPTURE");
            sendBroadcast(intent);


//            hideOverlay();
        });

        if (isOverlayVisible) {
            windowManager.addView(overlayView, params);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            boolean hideOverlay = intent.getBooleanExtra("hideOverlay", false);
            Log.d("OverlayOnStart", "Overlay value " + hideOverlay);

            if (hideOverlay) {
                hideOverlay();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    showOverlay();
                }
            }
        }
        return START_STICKY;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showOverlay() {
        if (!isOverlayVisible) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.TOP | Gravity.LEFT;
            params.x = 0;
            params.y = 100;

            windowManager.addView(overlayView, params);
            isOverlayVisible = true;
        }
    }

    private void hideOverlay() {
        if (isOverlayVisible) {
            windowManager.removeView(overlayView);
            isOverlayVisible = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);}

}
