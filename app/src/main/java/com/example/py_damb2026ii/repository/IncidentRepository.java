package com.example.py_damb2026ii.repository;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.R;
import com.example.py_damb2026ii.local.AppDatabase;
import com.example.py_damb2026ii.local.IncidenteEntity;
import com.example.py_damb2026ii.local.SolicitudPendienteEntity;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.example.py_damb2026ii.model.RptaObtenerActivoQR;
import com.example.py_damb2026ii.model.RptaObtenerIncidentes;
import com.example.py_damb2026ii.network.IncidentEntry;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IncidentRepository {

    private static IncidentRepository instance;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    public interface RepositoryCallback<T> {
        void onDataLoaded(T data, boolean isFromCache, String errorMessage);
        void onHttpError(int code);
    }

    public interface SyncCallback {
        void onSyncCompleted(int syncedCount, String extraMessage);
    }

    public static synchronized IncidentRepository getInstance() {
        if (instance == null) {
            instance = new IncidentRepository();
        }
        return instance;
    }

    public void cargarIncidentes(Context context, String jwt, String userCacheId, RepositoryCallback<List<IncidentEntry>> callback) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        executor.execute(() -> {
            List<IncidenteEntity> cached = AppDatabase.getInstance(appContext)
                    .incidenteDao().getIncidentesPorUsuario(userCacheId);
            List<SolicitudPendienteEntity> pendientes = AppDatabase.getInstance(appContext)
                    .solicitudPendienteDao().getPendientesPorUsuario(userCacheId);

            List<IncidentEntry> entries = mapearEntitiesYPendientesAEntries(cached, pendientes);
            if (!entries.isEmpty() && callback != null) {
                mainHandler.post(() -> callback.onDataLoaded(entries, true, null));
            }

            JCPyAnyApi api = new Retrofit.Builder()
                    .baseUrl(appContext.getString(R.string.dominioWS))
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(JCPyAnyApi.class);

            api.obtenerIncidentes("JWT " + jwt).enqueue(new Callback<RptaObtenerIncidentes>() {
                @Override
                public void onResponse(@NonNull Call<RptaObtenerIncidentes> call, @NonNull Response<RptaObtenerIncidentes> response) {
                    if (response.code() == 401) {
                        if (callback != null) {
                            mainHandler.post(() -> callback.onHttpError(401));
                        }
                        return;
                    }

                    if (!response.isSuccessful() || response.body() == null || response.body().getCode() != 1 || response.body().getData() == null) {
                        if (callback != null) {
                            mainHandler.post(() -> callback.onDataLoaded(entries, true, "HTTP " + response.code()));
                        }
                        return;
                    }

                    List<IncidentEntry> apiData = response.body().getData();
                    executor.execute(() -> {
                        List<IncidenteEntity> newEntities = mapearEntriesAEntities(apiData, userCacheId);
                        AppDatabase.getInstance(appContext).incidenteDao().deleteByUsuario(userCacheId);
                        AppDatabase.getInstance(appContext).incidenteDao().insertAll(newEntities);

                        List<SolicitudPendienteEntity> p = AppDatabase.getInstance(appContext)
                                .solicitudPendienteDao().getPendientesPorUsuario(userCacheId);
                        List<IncidentEntry> combined = mapearEntitiesYPendientesAEntries(newEntities, p);

                        if (callback != null) {
                            mainHandler.post(() -> callback.onDataLoaded(combined, false, null));
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<RptaObtenerIncidentes> call, @NonNull Throwable t) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onDataLoaded(entries, true, "Sin conexión con el servidor"));
                    }
                }
            });
        });
    }

    public void registrarIncidente(Context context, String jwt, String idUsuario,
                                   String titulo, String descripcion, String categoria, String prioridad,
                                   int idSucursal, Integer idActivo, String codigoQrActivo, List<Uri> fotos,
                                   RepositoryCallback<RptaInsertarIncidente> callback) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        JCPyAnyApi api = new Retrofit.Builder()
                .baseUrl(appContext.getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JCPyAnyApi.class);

        Map<String, RequestBody> campos = new HashMap<>();
        MediaType tipoTexto = MediaType.parse("text/plain");
        campos.put("titulo", RequestBody.create(tipoTexto, titulo));
        campos.put("descripcion", RequestBody.create(tipoTexto, descripcion));
        campos.put("categoria", RequestBody.create(tipoTexto, categoria));
        campos.put("prioridad", RequestBody.create(tipoTexto, prioridad));
        campos.put("id_sucursal", RequestBody.create(tipoTexto, String.valueOf(idSucursal)));
        if (idActivo != null) {
            campos.put("id_activo", RequestBody.create(tipoTexto, String.valueOf(idActivo)));
        }

        List<MultipartBody.Part> partesFotos = crearPartesFotosFromUris(appContext, fotos);

        api.insertarIncidente("JWT " + jwt, campos, partesFotos).enqueue(new Callback<RptaInsertarIncidente>() {
            @Override
            public void onResponse(@NonNull Call<RptaInsertarIncidente> call, @NonNull Response<RptaInsertarIncidente> response) {
                if (response.code() == 401) {
                    if (callback != null) {
                        mainHandler.post(() -> callback.onHttpError(401));
                    }
                    return;
                }

                if (!response.isSuccessful() || response.body() == null || response.body().getCode() != 1) {
                    String msg = "Error al registrar incidente";
                    if (response.body() != null && response.body().getMsge() != null) {
                        msg = response.body().getMsge();
                    }
                    final String finalMsg = msg;
                    final RptaInsertarIncidente body = response.body();
                    if (callback != null) {
                        mainHandler.post(() -> callback.onDataLoaded(body, false, finalMsg));
                    }
                    return;
                }

                final RptaInsertarIncidente body = response.body();
                if (callback != null) {
                    mainHandler.post(() -> callback.onDataLoaded(body, false, null));
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaInsertarIncidente> call, @NonNull Throwable t) {
                executor.execute(() -> {
                    guardarOffline(appContext, idUsuario, titulo, descripcion, categoria, prioridad, idSucursal, idActivo, codigoQrActivo, fotos);
                    if (callback != null) {
                        mainHandler.post(() -> callback.onDataLoaded(null, true, "Sin conexión: se enviará automáticamente al recuperar internet"));
                    }
                });
            }
        });
    }

    private void guardarOffline(Context context, String idUsuario, String titulo, String descripcion,
                                String categoria, String prioridad, int idSucursal, Integer idActivo, String codigoQrActivo, List<Uri> fotos) {
        try {
            File dir = new File(context.getFilesDir(), "pendientes");
            if (!dir.exists()) dir.mkdirs();

            String rutaFoto1 = null;
            String rutaFoto2 = null;

            if (fotos != null && !fotos.isEmpty()) {
                rutaFoto1 = copiarFotoALocal(context, dir, fotos.get(0), 1);
                if (fotos.size() > 1) {
                    rutaFoto2 = copiarFotoALocal(context, dir, fotos.get(1), 2);
                }
            }

            SolicitudPendienteEntity entity = new SolicitudPendienteEntity();
            entity.titulo = titulo;
            entity.descripcion = descripcion;
            entity.categoria = categoria;
            entity.prioridad = prioridad;
            entity.id_sucursal = idSucursal;
            entity.id_activo = idActivo;
            entity.codigo_qr_activo = codigoQrActivo;
            entity.ruta_foto1 = rutaFoto1;
            entity.ruta_foto2 = rutaFoto2;
            entity.id_usuario = idUsuario;
            entity.fecha_creacion = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            entity.estado = "Pendiente";

            AppDatabase.getInstance(context).solicitudPendienteDao().insert(entity);
            Log.d("USAT_APP", "Incidente guardado en Room de forma offline");
        } catch (Exception e) {
            Log.e("USAT_APP", "Error guardando incidente offline: " + e.getMessage());
        }
    }

    private String copiarFotoALocal(Context context, File dir, Uri uri, int indice) {
        try {
            File dest = new File(dir, "pending_" + System.currentTimeMillis() + "_" + indice + ".jpg");
            InputStream is = context.getContentResolver().openInputStream(uri);
            FileOutputStream fos = new FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) > 0) {
                fos.write(buf, 0, len);
            }
            fos.close();
            if (is != null) is.close();
            return dest.getAbsolutePath();
        } catch (Exception e) {
            Log.e("USAT_APP", "Error copiando foto local: " + e.getMessage());
            return null;
        }
    }

    public void sincronizarPendientes(Context context, String jwt, String idUsuario, SyncCallback callback) {
        if (context == null || jwt == null || jwt.isEmpty() || idUsuario == null || idUsuario.isEmpty()) return;
        if (!isSyncing.compareAndSet(false, true)) {
            Log.d("USAT_APP", "Sincronización ya está en curso, omitiendo ejecución simultánea.");
            return;
        }

        Context appContext = context.getApplicationContext();

        executor.execute(() -> {
            Log.d("USAT_APP", "Sincronización de solicitudes pendientes iniciada para usuario: " + idUsuario);
            int syncedCount = 0;
            String warningMsg = null;

            try {
                // Resetear cualquier solicitud atascada en "Enviando"
                AppDatabase.getInstance(appContext).solicitudPendienteDao().resetearEnviando();

                List<SolicitudPendienteEntity> pendientes = AppDatabase.getInstance(appContext)
                        .solicitudPendienteDao().getPendientesPorUsuario(idUsuario);

                if (pendientes == null || pendientes.isEmpty()) {
                    Log.d("USAT_APP", "No hay solicitudes pendientes por sincronizar.");
                    return;
                }

                Log.d("USAT_APP", "Solicitudes pendientes encontradas: " + pendientes.size());

                JCPyAnyApi api = new Retrofit.Builder()
                        .baseUrl(appContext.getString(R.string.dominioWS))
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create(JCPyAnyApi.class);

                for (SolicitudPendienteEntity sol : pendientes) {
                    AppDatabase.getInstance(appContext).solicitudPendienteDao().actualizarEstado(sol.id, "Enviando");

                    // Si hay un código QR de activo guardado offline pero no id_activo, consultar primero al servidor
                    if (sol.codigo_qr_activo != null && !sol.codigo_qr_activo.trim().isEmpty() && sol.id_activo == null) {
                        try {
                            Log.d("USAT_APP", "Consultando activo QR offline: " + sol.codigo_qr_activo);
                            Response<RptaObtenerActivoQR> resQR = api.obtenerActivoQR("JWT " + jwt, sol.codigo_qr_activo.trim()).execute();
                            if (resQR.code() == 401) {
                                Log.d("USAT_APP", "Token expirado durante sincronización de QR.");
                                return;
                            }
                            if (resQR.isSuccessful() && resQR.body() != null && resQR.body().getCode() == 1 && resQR.body().getData() != null) {
                                sol.id_activo = resQR.body().getData().id;
                                Log.d("USAT_APP", "Activo QR encontrado id=" + sol.id_activo);
                            } else {
                                warningMsg = "el activo " + sol.codigo_qr_activo + " no se encontró";
                                Log.d("USAT_APP", "Activo QR no encontrado: " + warningMsg);
                            }
                        } catch (Exception e) {
                            Log.d("USAT_APP", "Fallo de red consultando QR " + sol.codigo_qr_activo + ": " + e.getMessage());
                            AppDatabase.getInstance(appContext).solicitudPendienteDao().actualizarEstado(sol.id, "Pendiente");
                            return;
                        }
                    }

                    Map<String, RequestBody> campos = new HashMap<>();
                    MediaType tipoTexto = MediaType.parse("text/plain");
                    campos.put("titulo", RequestBody.create(tipoTexto, sol.titulo));
                    campos.put("descripcion", RequestBody.create(tipoTexto, sol.descripcion));
                    campos.put("categoria", RequestBody.create(tipoTexto, sol.categoria));
                    campos.put("prioridad", RequestBody.create(tipoTexto, sol.prioridad));
                    campos.put("id_sucursal", RequestBody.create(tipoTexto, String.valueOf(sol.id_sucursal)));
                    if (sol.id_activo != null) {
                        campos.put("id_activo", RequestBody.create(tipoTexto, String.valueOf(sol.id_activo)));
                    }

                    List<MultipartBody.Part> partesFotos = new ArrayList<>();
                    if (sol.ruta_foto1 != null) {
                        File f1 = new File(sol.ruta_foto1);
                        if (f1.exists()) {
                            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), f1);
                            partesFotos.add(MultipartBody.Part.createFormData("fotos", f1.getName(), body));
                        }
                    }
                    if (sol.ruta_foto2 != null) {
                        File f2 = new File(sol.ruta_foto2);
                        if (f2.exists()) {
                            RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), f2);
                            partesFotos.add(MultipartBody.Part.createFormData("fotos", f2.getName(), body));
                        }
                    }

                    try {
                        Log.d("USAT_APP", "Enviando solicitud pendiente id=" + sol.id + " titulo=" + sol.titulo);
                        Response<RptaInsertarIncidente> res = api.insertarIncidente("JWT " + jwt, campos, partesFotos).execute();
                        Log.d("USAT_APP", "Respuesta HTTP " + res.code() + " para solicitud id=" + sol.id);

                        if (res.code() == 401) {
                            Log.d("USAT_APP", "Token expirado durante envío.");
                            AppDatabase.getInstance(appContext).solicitudPendienteDao().actualizarEstado(sol.id, "Pendiente");
                            return;
                        }

                        if (res.isSuccessful() && res.body() != null && res.body().getCode() == 1) {
                            Log.d("USAT_APP", "Solicitud id=" + sol.id + " enviada correctamente. Eliminando de Room.");
                            if (sol.ruta_foto1 != null) new File(sol.ruta_foto1).delete();
                            if (sol.ruta_foto2 != null) new File(sol.ruta_foto2).delete();
                            AppDatabase.getInstance(appContext).solicitudPendienteDao().deleteById(sol.id);
                            syncedCount++;
                        } else {
                            String errServidor = (res.body() != null && res.body().getMsge() != null)
                                    ? res.body().getMsge() : "Error en servidor HTTP " + res.code();
                            Log.d("USAT_APP", "Servidor rechazo solicitud id=" + sol.id + ": " + errServidor);
                            AppDatabase.getInstance(appContext).solicitudPendienteDao().actualizarEstado(sol.id, "Error: " + errServidor);
                        }
                    } catch (Exception e) {
                        Log.d("USAT_APP", "Fallo de red enviando pendiente " + sol.id + ": " + e.getMessage());
                        AppDatabase.getInstance(appContext).solicitudPendienteDao().actualizarEstado(sol.id, "Pendiente");
                        break;
                    }
                }
            } finally {
                isSyncing.set(false);
            }

            if (syncedCount > 0 && callback != null) {
                final int finalCount = syncedCount;
                final String finalWarn = warningMsg;
                mainHandler.post(() -> callback.onSyncCompleted(finalCount, finalWarn));
            }
        });
    }

    private List<MultipartBody.Part> crearPartesFotosFromUris(Context context, List<Uri> uris) {
        List<MultipartBody.Part> partes = new ArrayList<>();
        if (uris == null) return partes;
        for (int i = 0; i < uris.size(); i++) {
            Uri uri = uris.get(i);
            try {
                InputStream is = context.getContentResolver().openInputStream(uri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) != -1) {
                    baos.write(buf, 0, len);
                }
                if (is != null) is.close();
                RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"), baos.toByteArray());
                partes.add(MultipartBody.Part.createFormData("fotos", "foto_" + (i + 1) + ".jpg", body));
            } catch (Exception ignored) {}
        }
        return partes;
    }

    private List<IncidentEntry> mapearEntitiesYPendientesAEntries(List<IncidenteEntity> entities, List<SolicitudPendienteEntity> pendientes) {
        List<IncidentEntry> list = new ArrayList<>();

        if (pendientes != null) {
            for (SolicitudPendienteEntity p : pendientes) {
                IncidentEntry entry = new IncidentEntry();
                entry.id = -p.id;
                entry.titulo = p.titulo;
                entry.descripcion = p.descripcion;
                entry.categoria = p.categoria;
                entry.prioridad = p.prioridad;
                entry.estado = (p.estado != null && p.estado.startsWith("Error"))
                        ? p.estado
                        : "Pendiente de sincronización";
                entry.id_sucursal = p.id_sucursal;
                entry.fecha_registro = p.fecha_creacion;
                list.add(entry);
            }
        }

        if (entities != null) {
            for (IncidenteEntity e : entities) {
                IncidentEntry entry = new IncidentEntry();
                entry.id = e.id;
                entry.titulo = e.titulo;
                entry.descripcion = e.descripcion;
                entry.categoria = e.categoria;
                entry.prioridad = e.prioridad;
                entry.estado = e.estado;
                entry.id_sucursal = e.id_sucursal;
                entry.fecha_registro = e.fecha_registro;
                entry.tecnico = e.tecnico;
                entry.solicitante = e.solicitante;
                entry.sucursal = e.sucursal;
                list.add(entry);
            }
        }
        return list;
    }

    private List<IncidenteEntity> mapearEntriesAEntities(List<IncidentEntry> entries, String userCacheId) {
        List<IncidenteEntity> list = new ArrayList<>();
        if (entries == null) return list;
        for (IncidentEntry e : entries) {
            IncidenteEntity entity = new IncidenteEntity();
            entity.id = e.id;
            entity.titulo = e.titulo;
            entity.descripcion = e.descripcion;
            entity.categoria = e.categoria;
            entity.prioridad = e.prioridad;
            entity.estado = e.estado;
            entity.id_sucursal = e.id_sucursal;
            entity.fecha_registro = e.fecha_registro;
            entity.tecnico = e.tecnico;
            entity.solicitante = e.solicitante;
            entity.sucursal = e.sucursal;
            entity.id_usuario_cache = userCacheId;
            list.add(entity);
        }
        return list;
    }
}
