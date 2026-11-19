package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentAddIncidentBinding;
import com.example.py_damb2026ii.model.RequInsertarIncidente;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.example.py_damb2026ii.model.RptaObtenerSucursales;
import com.example.py_damb2026ii.model.RptaObtenerActivoQR;
import com.example.py_damb2026ii.network.SucursalEntry;
import com.example.py_damb2026ii.util.QrHelper;
import com.example.py_damb2026ii.util.SesionHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import okhttp3.MultipartBody;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddIncidentFragment extends Fragment {

    private FragmentAddIncidentBinding binding;
    private SharedPreferences sharedPref;
    private List<SucursalEntry> listaSucursales;
    private SucursalEntry sucursalSeleccionada;
    private Integer idActivoSeleccionado = null;
    private String codigoQrPendiente = null;
    // ========================================
// FOTOGRAFIAS DEL INCIDENTE
// ========================================

    private static final int MAX_FOTOS = 2;
    private static final int MAX_TAMANO_FOTO = 5 * 1024 * 1024;

    private final List<Uri> fotosSeleccionadas = new ArrayList<>();

    private ActivityResultLauncher<String> selectorGaleria;

    private ActivityResultLauncher<Uri> tomarFoto;

    private Uri fotoTemporal;

    public AddIncidentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Seleccionar desde galería
        selectorGaleria = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {
                        agregarFoto(uri);
                    }
                }
        );

        // Tomar fotografía con cámara
        tomarFoto = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                resultado -> {

                    if (resultado && fotoTemporal != null) {

                        agregarFoto(fotoTemporal);

                    } else {

                        fotoTemporal = null;
                    }
                }
        );
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentAddIncidentBinding.inflate(inflater, container, false);

        // Botón para agregar fotografías
        binding.btnAgregarFoto.setOnClickListener(
                v -> mostrarOpcionesFoto()
        );

        actualizarVistaFotos();

        setupSpinners();
        cargarSucursales();

        // El solicitante no puede elegir otra sucursal
        binding.sucursalSpinner.setEnabled(false);

        // No puede registrar nuevas sucursales
        binding.btnIrRegistrarSucursal.setVisibility(View.GONE);

        binding.cardEscanearEquipo.setOnClickListener(v -> {
            if (idActivoSeleccionado != null || codigoQrPendiente != null) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Activo seleccionado")
                        .setMessage("¿Deseas desvincular el activo actual?")
                        .setPositiveButton("Quitar", (dialog, which) -> {
                            idActivoSeleccionado = null;
                            codigoQrPendiente = null;
                            binding.txtEstadoActivo.setText("Identifica el activo con QR · Toca para escanear");
                            Snackbar.make(binding.getRoot(), "Activo desvinculado", Snackbar.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            } else {
                QrHelper.escanearQr(requireContext(), new QrHelper.OnQrScannedListener() {
                    @Override
                    public void onScanned(String rawValue) {
                        consultarActivoQR(rawValue);
                    }
                    @Override public void onError(Exception e) {}
                    @Override public void onCancelled() {}
                });
            }
        });

        binding.addIncidentButton.setOnClickListener(v -> insertarIncidente(() -> {
            Snackbar.make(binding.getRoot(), "Incidente registrado correctamente", Snackbar.LENGTH_LONG)
                    .setAnchorView(requireActivity().findViewById(R.id.fab))
                    .setAction("Action", null).show();
            Navigation.findNavController(v).navigate(R.id.IncidentListFragment);
        }));

        binding.cancelAddIncidentButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return binding.getRoot();
    }

    private void setupSpinners() {
        // Categoría Spinner
        String[] categorias = {"Red", "Hardware", "Software", "Acceso", "Impresora", "Otro"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categorias);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.categoriaSpinner.setAdapter(catAdapter);

        binding.categoriaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = categorias[position];
                if ("Otro".equals(selected)) {
                    binding.categoriaCustomInput.setVisibility(View.VISIBLE);
                } else {
                    binding.categoriaCustomInput.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Prioridad Spinner (Semáforo)
        String[] prioridades = {"🔴 Alta", "🟡 Media", "🟢 Baja"};
        ArrayAdapter<String> prioAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, prioridades);
        prioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.prioridadSpinner.setAdapter(prioAdapter);
    }


    private void cargarSucursales() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");

        Call<RptaObtenerSucursales> call =
                api.obtenerSucursales("JWT " + token);

        call.enqueue(new Callback<RptaObtenerSucursales>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaObtenerSucursales> call,
                    @NonNull Response<RptaObtenerSucursales> response) {

                if (!isAdded() || binding == null) return;

                if (response.code() == 401) {
                    com.example.py_damb2026ii.util.SesionHelper.cerrarSesionPorVencimiento(requireContext());
                    return;
                }

                Log.d("USAT_APP", "HTTP sucursales: " + response.code());

                if (!response.isSuccessful() || response.body() == null || response.body().getCode() != 1 || response.body().getData() == null || response.body().getData().isEmpty()) {
                    usarSucursalOffline();
                    return;
                }

                listaSucursales = response.body().getData();
                sucursalSeleccionada = listaSucursales.get(0);
                sharedPref.edit()
                        .putInt("USER_ID_SUCURSAL", sucursalSeleccionada.id)
                        .putString("USER_SUCURSAL_NOMBRE", sucursalSeleccionada.nombre)
                        .apply();

                ArrayAdapter<SucursalEntry> adapter =
                        new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                listaSucursales
                        );

                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );

                binding.sucursalSpinner.setAdapter(adapter);

                binding.sucursalSpinner.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(
                                    AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {

                                sucursalSeleccionada =
                                        listaSucursales.get(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                sucursalSeleccionada = null;
                            }
                        }
                );

                Log.d("USAT_APP",
                        "Sucursales cargadas: " + listaSucursales.size());

            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaObtenerSucursales> call,
                    @NonNull Throwable throwable) {

                Log.e("USAT_APP",
                        "Error obteniendo sucursales (offline/red)", throwable);
                usarSucursalOffline();
            }
        });
    }

    private void usarSucursalOffline() {
        if (!isAdded() || binding == null) return;
        int idSaved = sharedPref.getInt("USER_ID_SUCURSAL", 1);
        String nombreSaved = sharedPref.getString("USER_SUCURSAL_NOMBRE", "Sucursal asignada");
        sucursalSeleccionada = new SucursalEntry(idSaved, nombreSaved, "", 0.0, 0.0, 1);
        List<SucursalEntry> list = new ArrayList<>();
        list.add(sucursalSeleccionada);
        ArrayAdapter<SucursalEntry> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                list
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sucursalSpinner.setAdapter(adapter);
    }


    private void insertarIncidente(Runnable transaccionOK) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        String token = sharedPref.getString("TOKEN_JWT", "");


        String titulo = binding.tituloEditText.getText() != null
                ? binding.tituloEditText.getText().toString().trim() : "";

        String descripcion = binding.descripcionEditText.getText() != null
                ? binding.descripcionEditText.getText().toString().trim() : "";

// Validar campos obligatorios
        if (titulo.isEmpty() || descripcion.isEmpty()) {
            Snackbar.make(binding.getRoot(),
                    "Complete el título y la descripción",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

// Validar sucursal
        if (sucursalSeleccionada == null) {
            usarSucursalOffline();
        }

// Obtener categoría
        String categoria = binding.categoriaSpinner
                .getSelectedItem().toString();

// La BD solo admite Red, Hardware, Software,
// Acceso, Impresora y Otro.
        if ("Otro".equals(categoria)) {
            String especificacion = binding.categoriaCustomEdit
                    .getText() != null
                    ? binding.categoriaCustomEdit.getText()
                    .toString().trim() : "";

            if (!especificacion.isEmpty()) {
                descripcion += "\nTipo de problema: " + especificacion;
            }
        }

// Obtener prioridad sin emojis
        String prioridadRaw = binding.prioridadSpinner
                .getSelectedItem().toString();

        String prioridad = "Media";

        if (prioridadRaw.contains("Alta")) {
            prioridad = "Alta";
        } else if (prioridadRaw.contains("Baja")) {
            prioridad = "Baja";
        }

// Preparar campos form-data
        Map<String, RequestBody> campos = new HashMap<>();

        MediaType tipoTexto = MediaType.parse("text/plain");

        campos.put("titulo",
                RequestBody.create(tipoTexto, titulo));

        campos.put("descripcion",
                RequestBody.create(tipoTexto, descripcion));

        campos.put("categoria",
                RequestBody.create(tipoTexto, categoria));

        campos.put("prioridad",
                RequestBody.create(tipoTexto, prioridad));

        if (idActivoSeleccionado != null) {
            campos.put("id_activo",
                    RequestBody.create(tipoTexto, String.valueOf(idActivoSeleccionado)));
        }



// Preparar fotografías para enviar
        List<MultipartBody.Part> partesFotos;

        try {

            partesFotos = crearPartesFotos();

        } catch (IOException e) {

            Snackbar.make(
                    binding.getRoot(),
                    e.getMessage(),
                    Snackbar.LENGTH_LONG
            ).show();

            return;
        }

// Evitar envíos duplicados
        binding.addIncidentButton.setEnabled(false);
        binding.addIncidentButton.setText("Enviando...");

        com.example.py_damb2026ii.repository.SupabaseIncidentRepository.INSTANCE.registrarIncidente(
                requireContext(),
                titulo,
                descripcion,
                categoria,
                prioridad,
                idActivoSeleccionado != null ? String.valueOf(idActivoSeleccionado) : null,
                new com.example.py_damb2026ii.repository.IncidentCallback() {
                    @Override
                    public void onSuccess(@NonNull String incidenteId) {
                        if (!isAdded() || binding == null) return;
                        binding.addIncidentButton.setEnabled(true);
                        binding.addIncidentButton.setText("Enviar incidente");
                        Snackbar.make(binding.getRoot(), "Incidente registrado exitosamente", Snackbar.LENGTH_SHORT).show();
                        if (transaccionOK != null) {
                            transaccionOK.run();
                        }
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        if (!isAdded() || binding == null) return;
                        binding.addIncidentButton.setEnabled(true);
                        binding.addIncidentButton.setText("Enviar incidente");
                        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
                    }
                },
                androidx.lifecycle.LifecycleOwnerKt.getLifecycleScope(this)
        );
    }

    private void mostrarOpcionesFoto() {

        if (fotosSeleccionadas.size() >= MAX_FOTOS) {

            Snackbar.make(
                    binding.getRoot(),
                    "Solo puedes adjuntar 2 fotografías.",
                    Snackbar.LENGTH_SHORT
            ).show();

            return;
        }

        String[] opciones = {
                "Tomar fotografía",
                "Elegir de galería"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Agregar fotografía")
                .setItems(opciones, (dialog, posicion) -> {

                    if (posicion == 0) {
                        abrirCamara();
                    } else {
                        abrirGaleria();
                    }

                })
                .show();
    }

    private void abrirCamara() {

        try {

            File directorio = new File(
                    requireContext().getCacheDir(),
                    "incident_photos"
            );

            if (!directorio.exists() && !directorio.mkdirs()) {
                throw new IOException("No se pudo crear el directorio");
            }

            File archivo = new File(
                    directorio,
                    "foto_" + System.currentTimeMillis() + ".jpg"
            );

            fotoTemporal = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().getPackageName() + ".fileprovider",
                    archivo
            );

            tomarFoto.launch(fotoTemporal);

        } catch (Exception e) {

            Log.e("FOTOS_INCIDENTE", "Error al abrir cámara", e);

            Snackbar.make(
                    binding.getRoot(),
                    "No se pudo abrir la cámara.",
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }

    private void abrirGaleria() {

        selectorGaleria.launch("image/*");
    }

    private void agregarFoto(Uri uri) {

        if (binding == null) return;

        if (fotosSeleccionadas.size() >= MAX_FOTOS) {

            Snackbar.make(
                    binding.getRoot(),
                    "Ya alcanzaste el límite de fotografías.",
                    Snackbar.LENGTH_SHORT
            ).show();

            return;
        }

        fotosSeleccionadas.add(uri);

        actualizarVistaFotos();

        Snackbar.make(
                binding.getRoot(),
                "Fotografía agregada.",
                Snackbar.LENGTH_SHORT
        ).show();
    }

    private void actualizarVistaFotos() {

        if (binding == null) return;

        LinearLayout contenedor = binding.fotosPreviewContainer;

        contenedor.removeAllViews();

        binding.fotosContador.setText(
                fotosSeleccionadas.size() + " / " + MAX_FOTOS
        );

        // Mostrar las fotografías seleccionadas
        for (int i = 0; i < fotosSeleccionadas.size(); i++) {

            Uri uri = fotosSeleccionadas.get(i);

            FrameLayout tarjeta = new FrameLayout(requireContext());

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            dp(76),
                            dp(80)
                    );

            parametros.setMarginEnd(dp(8));

            tarjeta.setLayoutParams(parametros);

            ImageView imagen = new ImageView(requireContext());

            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            GradientDrawable fondo = new GradientDrawable();
            fondo.setColor(Color.rgb(225, 239, 246));
            fondo.setCornerRadius(dp(14));

            imagen.setBackground(fondo);
            imagen.setClipToOutline(true);
            imagen.setImageURI(uri);

            tarjeta.addView(
                    imagen,
                    new FrameLayout.LayoutParams(
                            -1,
                            -1
                    )
            );

            // Botón X para eliminar
            TextView eliminar = new TextView(requireContext());

            eliminar.setText("×");
            eliminar.setTextColor(Color.WHITE);
            eliminar.setTextSize(17);
            eliminar.setGravity(Gravity.CENTER);

            GradientDrawable fondoEliminar = new GradientDrawable();
            fondoEliminar.setShape(GradientDrawable.OVAL);
            fondoEliminar.setColor(Color.rgb(48, 76, 91));

            eliminar.setBackground(fondoEliminar);

            FrameLayout.LayoutParams parametrosEliminar =
                    new FrameLayout.LayoutParams(
                            dp(22),
                            dp(22),
                            Gravity.TOP | Gravity.END
                    );

            parametrosEliminar.setMargins(
                    0,
                    dp(3),
                    dp(3),
                    0
            );

            tarjeta.addView(eliminar, parametrosEliminar);

            eliminar.setOnClickListener(v -> {

                fotosSeleccionadas.remove(uri);

                actualizarVistaFotos();
            });

            contenedor.addView(tarjeta);
        }

        // Actualizar el recuadro para agregar fotografía
        boolean disponible =
                fotosSeleccionadas.size() < MAX_FOTOS;

        binding.btnAgregarFoto.setEnabled(disponible);

        binding.btnAgregarFoto.setAlpha(
                disponible ? 1f : 0.55f
        );

        binding.txtAgregarFoto.setText(
                disponible ? "Añadir" : "Límite"
        );

        binding.iconoAgregarFoto.setVisibility(
                View.VISIBLE
        );

        // El recuadro siempre queda visible
        contenedor.addView(binding.btnAgregarFoto);
    }

    private int dp(int valor) {

        return (int) (
                valor * getResources()
                        .getDisplayMetrics().density
        );
    }



    private byte[] comprimirFoto(Uri uri) throws IOException {

        BitmapFactory.Options opciones =
                new BitmapFactory.Options();

        opciones.inJustDecodeBounds = true;

        try (InputStream entrada =
                     requireContext().getContentResolver()
                             .openInputStream(uri)) {

            BitmapFactory.decodeStream(
                    entrada, null, opciones
            );
        }

        if (opciones.outWidth <= 0 || opciones.outHeight <= 0) {
            throw new IOException("La imagen no es válida.");
        }

        // Limitar resolución a 1600 píxeles
        int muestra = 1;

        while (
                opciones.outWidth / muestra > 1600 ||
                        opciones.outHeight / muestra > 1600
        ) {
            muestra *= 2;
        }

        opciones.inJustDecodeBounds = false;
        opciones.inSampleSize = muestra;

        Bitmap bitmap;

        try (InputStream entrada =
                     requireContext().getContentResolver()
                             .openInputStream(uri)) {

            bitmap = BitmapFactory.decodeStream(
                    entrada, null, opciones
            );
        }

        if (bitmap == null) {
            throw new IOException("No se pudo procesar la fotografía.");
        }

        ByteArrayOutputStream salida =
                new ByteArrayOutputStream();

        int[] calidades = {85, 75, 65};

        for (int calidad : calidades) {

            salida.reset();

            bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    calidad,
                    salida
            );

            if (salida.size() <= MAX_TAMANO_FOTO) {
                break;
            }
        }

        bitmap.recycle();

        if (salida.size() > MAX_TAMANO_FOTO) {
            throw new IOException(
                    "La fotografía supera el límite de 5 MB."
            );
        }

        return salida.toByteArray();
    }


    private List<MultipartBody.Part> crearPartesFotos()
            throws IOException {

        List<MultipartBody.Part> partes =
                new ArrayList<>();

        for (int i = 0; i < fotosSeleccionadas.size(); i++) {

            Uri uri = fotosSeleccionadas.get(i);

            byte[] imagen = comprimirFoto(uri);

            RequestBody cuerpo = RequestBody.create(
                    MediaType.parse("image/jpeg"),
                    imagen
            );

            MultipartBody.Part parte =
                    MultipartBody.Part.createFormData(
                            "fotos",
                            "incidente_" + (i + 1) + ".jpg",
                            cuerpo
                    );

            partes.add(parte);
        }

        return partes;
    }

    private void consultarActivoQR(String codigoQR) {
        JCPyAnyApi api = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");
        api.obtenerActivoQR("JWT " + token, codigoQR).enqueue(new Callback<RptaObtenerActivoQR>() {
            @Override
            public void onResponse(@NonNull Call<RptaObtenerActivoQR> call, @NonNull Response<RptaObtenerActivoQR> response) {
                if (!isAdded() || binding == null) return;
                if (response.code() == 401) {
                    SesionHelper.cerrarSesionPorVencimiento(requireContext());
                    return;
                }

                if (!response.isSuccessful() || response.body() == null) {
                    String errorMsg = "Activo no encontrado";
                    try {
                        if (response.errorBody() != null) {
                            RptaObtenerActivoQR errBody = new Gson().fromJson(response.errorBody().charStream(), RptaObtenerActivoQR.class);
                            if (errBody != null && errBody.getMsge() != null) {
                                errorMsg = errBody.getMsge();
                            }
                        }
                    } catch (Exception ignored) {}
                    Snackbar.make(binding.getRoot(), errorMsg, Snackbar.LENGTH_LONG).show();
                    return;
                }

                RptaObtenerActivoQR rpta = response.body();
                if (rpta.getCode() == 1 && rpta.getData() != null) {
                    idActivoSeleccionado = rpta.getData().id;
                    binding.txtEstadoActivo.setText(rpta.getData().nombre + " (" + rpta.getData().tipo + ")");
                    Snackbar.make(binding.getRoot(), "Activo vinculado correctamente", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(binding.getRoot(), rpta.getMsge() != null ? rpta.getMsge() : "Activo no encontrado", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaObtenerActivoQR> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                codigoQrPendiente = codigoQR;
                idActivoSeleccionado = null;
                binding.txtEstadoActivo.setText("Activo: " + codigoQR + " (se validará al sincronizar)");
            }
        });
    }

}
