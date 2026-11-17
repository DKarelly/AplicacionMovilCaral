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
        // La sesión es gestionada por Supabase Auth.
        // Se omiten los cierres de sesión automáticos generados por el backend antiguo (PythonAnywhere).
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
