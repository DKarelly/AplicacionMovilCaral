package com.example.py_damb2026ii.application;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public class UsatApplication extends Application {
    private static UsatApplication instance;
    private static Context appContext;

    public static UsatApplication getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return appContext;
    }

    public void setAppContext(Context mAppContext) {
        this.appContext = mAppContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        this.setAppContext(getApplicationContext());
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        crearCanalNotificaciones();
    }

    private void crearCanalNotificaciones() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "incidentes",
                    "Notificaciones de Incidentes",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Canal para notificaciones push de incidentes");
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
