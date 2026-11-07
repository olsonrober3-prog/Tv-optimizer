package com.tvoptimizer;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.StatFs;
import android.os.Environment;
import android.provider.Settings;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView stats;
    private final int background = Color.rgb(14, 20, 32);
    private final int accent = Color.rgb(35, 116, 190);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(24), dp(18), dp(24), dp(24));
        root.setBackgroundColor(background);

        TextView title = new TextView(this);
        title.setText("TV OPTIMIZER");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView subtitle = new TextView(this);
        subtitle.setText("Estado de tu dispositivo");
        subtitle.setTextColor(Color.rgb(130, 190, 245));
        subtitle.setTextSize(17);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(34)));

        stats = new TextView(this);
        stats.setTextColor(Color.rgb(225, 235, 248));
        stats.setTextSize(18);
        stats.setPadding(0, dp(10), 0, dp(18));
        root.addView(stats, new LinearLayout.LayoutParams(-1, -2));

        addButton("↻   Actualizar diagnóstico", this::refreshStats);
        addButton("▣   Administrar aplicaciones", () ->
            startActivity(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)));
        addButton("▤   Almacenamiento", () ->
            startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)));
        addButton("⚙   Ajustes del dispositivo", () ->
            startActivity(new Intent(Settings.ACTION_SETTINGS)));
        addButton("☷   Ver aplicaciones instaladas", this::showApps);

        TextView note = new TextView(this);
        note.setText("TV Optimizer • Herramientas de diagnóstico");
        note.setTextColor(Color.rgb(145, 155, 175));
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, dp(48));
        noteParams.topMargin = dp(10);
        root.addView(note, noteParams);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(background);
        scroll.addView(root);
        setContentView(scroll);
        refreshStats();
    }

    private int dp(int value) {
        return (int)(value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void addButton(String label, Runnable action) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setFocusable(true);
        b.setMinHeight(dp(58));
        b.setBackgroundTintList(ColorStateList.valueOf(accent));
        b.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, dp(62));
        p.setMargins(0, dp(6), 0, dp(6));
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
        list.setPadding(0, dp(16), 0, dp(16));
        list.setText(sb.toString());
        root.addView(list);
    }
}