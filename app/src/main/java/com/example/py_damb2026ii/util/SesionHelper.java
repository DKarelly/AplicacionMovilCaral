package com.example.py_damb2026ii.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.example.py_damb2026ii.LoginActivity;
import com.example.py_damb2026ii.UbicacionTecnicoService;

public class SesionHelper {

    public static void cerrarSesionPorVencimiento(Context ctx) {
        if (ctx == null) return;

        // 1. Borrar SharedPreferences "USAT_SP"
        SharedPreferences prefs = ctx.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
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
}
