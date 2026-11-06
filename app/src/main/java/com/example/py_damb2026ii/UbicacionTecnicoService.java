
package com.example.py_damb2026ii;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UbicacionTecnicoService extends Service {

    public static final String ACTION_INICIAR =
            "com.example.py_damb2026ii.INICIAR_GPS";

    public static final String ACTION_DETENER =
            "com.example.py_damb2026ii.DETENER_GPS";

    public static final String EXTRA_ID_INCIDENTE = "id_incidente";
    public static final String EXTRA_TOKEN = "token";

    private static final String CHANNEL_ID = "canal_ubicacion_tecnico";
    private static final int NOTIFICATION_ID = 7301;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    private int idIncidenteActual = -1;
    private String token = "";

    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        prefs = getSharedPreferences("USAT_SP", MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent != null ? intent.getAction() : ACTION_INICIAR;

        // DETENER SEGUIMIENTO
        if (ACTION_DETENER.equals(action)) {

            int idSolicitado = intent.getIntExtra(
                    EXTRA_ID_INCIDENTE, -1
            );

            if (idSolicitado > 0
                    && idIncidenteActual > 0
                    && idSolicitado != idIncidenteActual) {

                return START_NOT_STICKY;
            }

            detenerSeguimiento(true);
            stopSelf();

            return START_NOT_STICKY;
        }

        // RECUPERAR DATOS DEL INCIDENTE
        if (intent != null) {

            idIncidenteActual = intent.getIntExtra(
                    EXTRA_ID_INCIDENTE, -1
            );

            token = intent.getStringExtra(EXTRA_TOKEN);

            if (idIncidenteActual > 0) {
                prefs.edit()
                        .putInt("GPS_ID_INCIDENTE", idIncidenteActual)
                        .apply();
            }

        } else {

            idIncidenteActual = prefs.getInt(
                    "GPS_ID_INCIDENTE", -1
            );

            token = prefs.getString("TOKEN_JWT", "");
        }

        if (idIncidenteActual <= 0 || token == null || token.isEmpty()) {
            stopSelf();
            return START_NOT_STICKY;
        }

        crearCanalNotificacion();

        Notification notificacion = crearNotificacion();

        // Iniciar servicio en primer plano
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            startForeground(
                    NOTIFICATION_ID,
                    notificacion,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            );

        } else {

            startForeground(NOTIFICATION_ID, notificacion);
        }

        iniciarActualizacionesGPS();

        return START_STICKY;
    }

    private void iniciarActualizacionesGPS() {

        boolean permisoPreciso =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean permisoAproximado =
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (!permisoPreciso && !permisoAproximado) {

            detenerSeguimiento(true);
            stopSelf();
            return;
        }

        LocationRequest solicitud = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                30000L
        )
                .setMinUpdateIntervalMillis(15000L)
                .setMinUpdateDistanceMeters(15f)
                .build();

        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        locationCallback = new LocationCallback() {

            @Override
            public void onLocationResult(LocationResult resultado) {

                if (resultado == null) return;

                Location ubicacion = resultado.getLastLocation();

                if (ubicacion == null) return;

                enviarUbicacion(ubicacion);
            }
        };

        try {

            fusedLocationClient.requestLocationUpdates(
                    solicitud,
                    locationCallback,
                    Looper.getMainLooper()
            );

        } catch (SecurityException e) {

            Log.e("GPS_TECNICO", "Permiso GPS no disponible", e);

            detenerSeguimiento(true);
            stopSelf();
        }
    }

    private void enviarUbicacion(Location ubicacion) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        Map<String, Object> datos = new HashMap<>();

        datos.put("id_incidente", idIncidenteActual);
        datos.put("latitud", ubicacion.getLatitude());
        datos.put("longitud", ubicacion.getLongitude());
        datos.put("precision_m", ubicacion.getAccuracy());

        Call<RptaInsertarIncidente> llamada =
                api.actualizarUbicacionTecnico(
                        "JWT " + token,
                        datos
                );

        llamada.enqueue(new Callback<RptaInsertarIncidente>() {

            @Override
            public void onResponse(
                    Call<RptaInsertarIncidente> call,
                    Response<RptaInsertarIncidente> response) {

                if (!response.isSuccessful()) {

                    Log.e(
                            "GPS_TECNICO",
                            "Error HTTP: " + response.code()
                    );

                    // El servidor indica que el incidente
                    // ya no está en proceso.
                    if (response.code() == 409) {
                        detenerSeguimiento(true);
                        stopSelf();
                    }

                    return;
                }

                RptaInsertarIncidente rpta = response.body();

                if (rpta != null && rpta.getCode() == 1) {

                    Log.d(
                            "GPS_TECNICO",
                            "Ubicación enviada correctamente"
                    );

                } else {

                    Log.e(
                            "GPS_TECNICO",
                            "No se pudo guardar la ubicación"
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<RptaInsertarIncidente> call,
                    Throwable throwable) {

                Log.e(
                        "GPS_TECNICO",
                        "Error de conexión al enviar ubicación",
                        throwable
                );
            }
        });
    }

    private void crearCanalNotificacion() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID,
                    "Ubicación del técnico",
                    NotificationManager.IMPORTANCE_LOW
            );

            canal.setDescription(
                    "Indica cuándo se está compartiendo la ubicación"
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(canal);
        }
    }

    private Notification crearNotificacion() {

        String codigo = String.format(
                java.util.Locale.getDefault(),
                "INC-%03d",
                idIncidenteActual
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setContentTitle("Compartiendo ubicación")
                .setContentText(
                        "Ubicación activa durante la atención de " + codigo
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void detenerSeguimiento(boolean limpiarDatos) {

        if (fusedLocationClient != null && locationCallback != null) {

            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        stopForeground(true);

        if (limpiarDatos) {

            prefs.edit()
                    .remove("GPS_ID_INCIDENTE")
                    .apply();
        }
    }

    @Override
    public void onDestroy() {

        if (fusedLocationClient != null && locationCallback != null) {

            fusedLocationClient.removeLocationUpdates(locationCallback);
        }

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
