package com.tvoptimizer;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.BatteryManager;
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
import android.widget.Toast;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView stats;
    private final int background = Color.rgb(14, 20, 32);
    private final int accent = Color.rgb(35, 116, 190);
    private final String youtubePackage = "com.google.android.youtube";

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
        title.setText("TV OPTIMIZER 2.0");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView subtitle = new TextView(this);
        subtitle.setText("Diagnóstico y ayuda para mejorar tu dispositivo");
        subtitle.setTextColor(Color.rgb(130, 190, 245));
        subtitle.setTextSize(16);
        root.addView(subtitle, new LinearLayout.LayoutParams(-1, dp(42)));

        stats = new TextView(this);
        stats.setTextColor(Color.rgb(225, 235, 248));
        stats.setTextSize(17);
        stats.setPadding(0, dp(12), 0, dp(18));
        root.addView(stats, new LinearLayout.LayoutParams(-1, -2));

        addButton("↻   Actualizar diagnóstico", this::refreshStats);
        addButton("▶   Abrir YouTube", this::openYouTube);
        addButton("▶   Ayuda: YouTube se traba", this::showYouTubeHelp);
        addButton("▣   Ajustes de YouTube / borrar caché", this::openYouTubeSettings);
        addButton("◉   Ajustes de Wi-Fi e Internet", () ->
            safeSettings(new Intent(Settings.ACTION_WIFI_SETTINGS)));
        addButton("▣   Administrar aplicaciones", () ->
            safeSettings(new Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)));
        addButton("▤   Almacenamiento", () ->
            safeSettings(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)));
        addButton("▰   Batería y ahorro de energía", () ->
            safeSettings(new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)));
        addButton("⚙   Ajustes del dispositivo", () ->
            safeSettings(new Intent(Settings.ACTION_SETTINGS)));
        addButton("☷   Ver aplicaciones instaladas", this::showApps);

        TextView note = new TextView(this);
        note.setText("TV Optimizer • Diagnóstico seguro: no elimina archivos ni cierra apps a la fuerza");
        note.setTextColor(Color.rgb(145, 155, 175));
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, dp(14), 0, dp(14));
        root.addView(note, new LinearLayout.LayoutParams(-1, -2));

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

    private void safeSettings(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Esta opción no está disponible en este dispositivo", Toast.LENGTH_LONG).show();
        }
    }

    private String networkStatus() {
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network network = cm.getActiveNetwork();
            if (network == null) return "Sin conexión activa";
            NetworkCapabilities caps = cm.getNetworkCapabilities(network);
            if (caps == null) return "Conexión no identificada";
            String kind = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "Wi-Fi" :
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "Datos móviles" :
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ? "Ethernet" : "Otra conexión";
            return kind + (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ? " • Internet disponible" : " • Internet sin verificar");
        } catch (Exception e) {
            return "Estado de red no disponible";
        }
    }

    private void refreshStats() {
        ActivityManager am = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        StatFs fs = new StatFs(Environment.getDataDirectory().getPath());
        long total = fs.getTotalBytes();
        long free = fs.getAvailableBytes();

        Intent battery = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        String batteryText = "Batería: no disponible";
        if (battery != null) {
            int level = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
            int temp = battery.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
            if (level >= 0 && scale > 0) {
                batteryText = "Batería: " + (level * 100 / scale) + "%";
                if (temp >= 0) batteryText += String.format(Locale.getDefault(), " • %.1f °C", temp / 10.0);
            }
        }

        String s = String.format(Locale.getDefault(),
            "RAM disponible: %.0f MB de %.0f MB\nAlmacenamiento libre: %.1f GB de %.1f GB\n%s\nRed: %s",
            mi.availMem / 1048576.0, mi.totalMem / 1048576.0,
            free / 1073741824.0, total / 1073741824.0,
            batteryText, networkStatus());
        stats.setText(s);
    }

    private void openYouTube() {
        Intent launch = getPackageManager().getLaunchIntentForPackage(youtubePackage);
        if (launch == null) {
            Toast.makeText(this, "YouTube no está instalado. Puedes instalarlo desde Play Store.", Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(launch);
    }

    private void openYouTubeSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + youtubePackage));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No se encontraron los ajustes de YouTube", Toast.LENGTH_LONG).show();
        }
    }

    private void showYouTubeHelp() {
        new AlertDialog.Builder(this)
            .setTitle("Cómo evitar que YouTube se trabe")
            .setMessage("TV Optimizer no puede aumentar la velocidad contratada de Internet ni modificar internamente YouTube, pero estos pasos suelen ayudar:\n\n1. Comprueba que el Wi-Fi tenga buena señal.\n2. Pausa descargas y otros vídeos en la red.\n3. En YouTube, baja la calidad a 720p o 480p si el vídeo se detiene.\n4. En Ajustes de YouTube, entra en Almacenamiento y pulsa Borrar caché (no Borrar datos).\n5. Actualiza YouTube desde Play Store y reinicia el teléfono si sigue lento.")
            .setNegativeButton("Cerrar", null)
            .setPositiveButton("Abrir ajustes de YouTube", (d, w) -> openYouTubeSettings())
            .show();
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