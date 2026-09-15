package com.ghostblocker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            Intent i = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(i);
            Toast.makeText(this, "Izinkan Display over other apps, lalu buka lagi",
                Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        startForegroundService(new Intent(this, OverlayService.class));
        Toast.makeText(this, "GhostBlocker aktif", Toast.LENGTH_SHORT).show();
        finish();
    }
}
