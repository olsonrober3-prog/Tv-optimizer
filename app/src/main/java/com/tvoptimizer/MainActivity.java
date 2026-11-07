package com.tvoptimizer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StatFs;
import android.os.Environment;
import android.provider.Settings;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView stats;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(36, 24, 36, 24);
        root.setBackgroundColor(Color.rgb(14, 20, 32));

        TextView title = new TextView(this);
        title.setText("TV OPTIMIZER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, 70));

        stats = new TextView(this);
        stats.setTextColor(Color.rgb(205, 220, 240));
        stats.setTextSize(18);
        stats.setPadding(0, 12, 0, 18);
        root.addView(stats);

        addButton("Actualizar diagnóstico", this::refreshStats);
        addButton("Administrar aplicaciones", () ->
            startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)));
        addButton("Almacenamiento", () ->
            startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)));
        addButton("Ajustes del dispositivo", () ->
            startActivity(new Intent(Settings.ACTION_SETTINGS)));
        addButton("Ver aplicaciones instaladas", this::showApps);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(root);
        setContentView(scroll);
        refreshStats();
    }

    private void addButton(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(17);
        b.setAllCaps(false);
        b.setFocusable(true);
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 64);
        p.setMargins(0, 7, 0, 7);
        root.addView(b, p);
    }

    private void refreshStats() {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
        long total = fs.getTotalBytes();
        long free = fs.getAvailableBytes();
        String s = String.format(Locale.getDefault(),
            "Memoria RAM disponible: %.0f MB de %.0f MB\nAlmacenamiento libre: %.1f GB de %.1f GB",
            mi.availMem / 1048576.0, mi.totalMem / 1048576.0,
            free / 1073741824.0, total / 1073741824.0);
        stats.setText(s);
    }

    private void showApps() {
        StringBuilder sb = new StringBuilder("APLICACIONES INSTALADAS\n\n");
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        for (ApplicationInfo app : apps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                sb.append(pm.getApplicationLabel(app)).append("\n");
            }
        }
        TextView list = new TextView(this);
        list.setTextColor(Color.WHITE);
        list.setTextSize(16);
        list.setText(sb.toString());
        root.addView(list);
    }
}
