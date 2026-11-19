package com.example.py_damb2026ii.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.R;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SesionHelper {

    public static void cerrarSesionPorVencimiento(Context ctx) {
        if (ctx == null) return;
        // La sesión es gestionada por Supabase Auth.
        // Se omiten los cierres de sesión automáticos generados por el backend antiguo (PythonAnywhere).
    }

    public static boolean puedeGestionarIncidentes(SharedPreferences prefs) {
        if (prefs == null) return false;
        String rol = prefs.getString("USER_ROLE", "");
        boolean puedeGestionar = prefs.getBoolean("PUEDE_GESTIONAR_INCIDENTES", false);
        return "ADMIN_GLOBAL".equalsIgnoreCase(rol) ||
               "ADMIN_EMPRESA".equalsIgnoreCase(rol) ||
               ("EMPLEADO".equalsIgnoreCase(rol) && puedeGestionar);
    }

    public static boolean esAreaAutorizada(SharedPreferences prefs, String areaIncidente) {
        if (prefs == null) return false;
        String rol = prefs.getString("USER_ROLE", "");
        if ("ADMIN_GLOBAL".equalsIgnoreCase(rol) || "ADMIN_EMPRESA".equalsIgnoreCase(rol)) return true;
        String areasStr = prefs.getString("AREAS_AUTORIZADAS", "");
        if (areasStr.isEmpty()) return true;
        if (areaIncidente == null || areaIncidente.trim().isEmpty()) return true;
        String[] areas = areasStr.split(",");
        for (String a : areas) {
            if (a.trim().equalsIgnoreCase(areaIncidente.trim())) {
                return true;
            }
        }
        return false;
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
