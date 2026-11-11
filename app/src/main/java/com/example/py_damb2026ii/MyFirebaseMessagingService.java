package com.example.py_damb2026ii;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "Nuevo token FCM: " + token);

        SharedPreferences prefs = getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        prefs.edit().putString("FCM_TOKEN", token).apply();

        String jwt = prefs.getString("TOKEN_JWT", "");
        if (!jwt.isEmpty()) {
            enviarTokenServidor(jwt, token);
        }
    }

    private void enviarTokenServidor(String jwt, String fcmToken) {
        JCPyAnyApi api = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JCPyAnyApi.class);

        Map<String, String> body = new HashMap<>();
        body.put("fcm_token", fcmToken);

        api.registrarFcmToken("JWT " + jwt, body).enqueue(new Callback<RptaInsertarIncidente>() {
            @Override
            public void onResponse(@NonNull Call<RptaInsertarIncidente> call, @NonNull Response<RptaInsertarIncidente> response) {
                if (response.isSuccessful()) {
                    Log.d("FCM_TOKEN", "Token FCM registrado en el servidor exitosamente");
                } else {
                    Log.e("FCM_TOKEN", "Error al registrar FCM token en servidor: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaInsertarIncidente> call, @NonNull Throwable t) {
                Log.e("FCM_TOKEN", "Fallo de red al registrar FCM token: " + t.getMessage());
            }
        });
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();
        if (data == null || data.isEmpty()) return;

        String titulo = data.get("titulo");
        String cuerpo = data.get("cuerpo");
        String idIncidenteStr = data.get("id_incidente");

        if (titulo == null) titulo = "IncidentApp";
        if (cuerpo == null) cuerpo = "Tienes una nueva notificación de incidente.";

        int idIncidente = 0;
        try {
            if (idIncidenteStr != null) {
                idIncidente = Integer.parseInt(idIncidenteStr);
            }
        } catch (NumberFormatException ignored) {}

        SharedPreferences prefs = getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        if (prefs.getString("TOKEN_JWT", "").isEmpty() && idIncidente > 0) {
            prefs.edit().putInt("PENDING_INCIDENT_ID", idIncidente).apply();
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("id_incidente", idIncidente);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "incidentes")
                .setSmallIcon(R.drawable.usat_app_logo)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        try {
            notificationManager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException ignored) {}
    }
}
