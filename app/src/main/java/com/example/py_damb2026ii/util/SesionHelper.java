package com.example.py_damb2026ii.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.LoginActivity;
import com.example.py_damb2026ii.R;
import com.example.py_damb2026ii.UbicacionTecnicoService;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SesionHelper {

    public static void cerrarSesionPorVencimiento(Context ctx) {
        if (ctx == null) return;

        SharedPreferences prefs = ctx.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        String jwt = prefs.getString("TOKEN_JWT", "");

        if (!jwt.isEmpty()) {
            eliminarFcmTokenServidor(ctx, jwt);
        }

        try {
            FirebaseMessaging.getInstance().deleteToken();
        } catch (Exception ignored) {}

        final String userCacheId = prefs.getString("USER_NAME", "");
        final Context appContext = ctx.getApplicationContext();
        java.util.concurrent.Executors.newSingleThreadExecutor().execute(() -> {
            try {
                if (!userCacheId.isEmpty()) {
                    com.example.py_damb2026ii.local.AppDatabase.getInstance(appContext).incidenteDao().deleteByUsuario(userCacheId);
                    com.example.py_damb2026ii.local.AppDatabase.getInstance(appContext).historialDao().deleteByUsuario(userCacheId);
                } else {
                    com.example.py_damb2026ii.local.AppDatabase.getInstance(appContext).incidenteDao().deleteAll();
                    com.example.py_damb2026ii.local.AppDatabase.getInstance(appContext).historialDao().deleteAll();
                }
            } catch (Exception ignored) {}
        });

        // 1. Borrar SharedPreferences "USAT_SP"
        prefs.edit().clear().apply();

        // 2. Detener UbicacionTecnicoService si está en ejecución
        try {
            Intent serviceIntent = new Intent(ctx, UbicacionTecnicoService.class);
            ctx.stopService(serviceIntent);
        } catch (Exception ignored) {}

        // 3. Mostrar Toast
        Toast.makeText(ctx, "Tu sesión expiró. Inicia sesión nuevamente.", Toast.LENGTH_LONG).show();

        // 4. Abrir LoginActivity limpiando el stack de actividades
        Intent intent = new Intent(ctx, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ctx.startActivity(intent);
    }

    public static void eliminarFcmTokenServidor(Context ctx, String jwtToken) {
        try {
            JCPyAnyApi api = new Retrofit.Builder()
                    .baseUrl(ctx.getString(R.string.dominioWS))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(JCPyAnyApi.class);

            api.eliminarFcmToken("JWT " + jwtToken).enqueue(new Callback<RptaInsertarIncidente>() {
                @Override
                public void onResponse(@NonNull Call<RptaInsertarIncidente> call, @NonNull Response<RptaInsertarIncidente> response) {}
                @Override
                public void onFailure(@NonNull Call<RptaInsertarIncidente> call, @NonNull Throwable t) {}
            });
        } catch (Exception ignored) {}
    }
}
