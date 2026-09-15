package com.ghostblocker;

import android.app.*;
import android.content.*;
import android.graphics.PixelFormat;
import android.os.*;
import android.view.*;
import java.util.*;

public class OverlayService extends Service {

    WindowManager wm;
    List<View> views = new ArrayList<>();
    BroadcastReceiver receiver;

    @Override public IBinder onBind(Intent i) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        String chId = "gb";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(
                chId, "GhostBlocker", NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                .createNotificationChannel(ch);
        }
        Notification n = new Notification.Builder(this, chId)
            .setContentTitle("GhostBlocker")
            .setContentText("Aktif - blokir area ghost")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build();
        startForeground(1, n);

        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                String a = i.getAction();
                if ("com.ghostblocker.ADD".equals(a)) {
                    addArea(i.getIntExtra("x", 0), i.getIntExtra("y", 0),
                            i.getIntExtra("w", 100), i.getIntExtra("h", 100),
                            i.getBooleanExtra("visible", false));
                } else if ("com.ghostblocker.CLEAR".equals(a)) {
                    clearAreas();
                } else if ("com.ghostblocker.STOP".equals(a)) {
                    stopSelf();
                }
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction("com.ghostblocker.ADD");
        f.addAction("com.ghostblocker.CLEAR");
        f.addAction("com.ghostblocker.STOP");
        registerReceiver(receiver, f);
    }

    void addArea(int x, int y, int w, int h, boolean visible) {
        View v = new View(this);
        v.setBackgroundColor(visible ? 0xFF000000 : 0x00000000);

        int type = Build.VERSION.SDK_INT >= 26
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams p = new WindowManager.LayoutParams(
            w, h, x, y, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        );
        p.gravity = Gravity.TOP | Gravity.LEFT;

        try {
            wm.addView(v, p);
            views.add(v);
        } catch (Exception e) {}
    }

    void clearAreas() {
        for (View v : views) {
            try { wm.removeView(v); } catch (Exception e) {}
        }
        views.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearAreas();
        if (receiver != null) unregisterReceiver(receiver);
    }
}
