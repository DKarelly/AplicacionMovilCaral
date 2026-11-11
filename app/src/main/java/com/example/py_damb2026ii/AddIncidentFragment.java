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
import com.example.py_damb2026ii.network.SucursalEntry;
import com.google.android.material.snackbar.Snackbar;

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

                if (!response.isSuccessful() || response.body() == null) {
                    Snackbar.make(binding.getRoot(),
                            "Error al consultar sucursales. HTTP " + response.code(),
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                RptaObtenerSucursales rpta = response.body();

                Log.d("USAT_APP", "Código API: " + rpta.getCode());
                Log.d("USAT_APP", "Mensaje: " + rpta.getMsge());

                if (rpta.getCode() != 1) {
                    Snackbar.make(binding.getRoot(),
                            "Error: " + rpta.getMsge(),
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                listaSucursales = rpta.getData();

                if (listaSucursales == null || listaSucursales.isEmpty()) {
                    sucursalSeleccionada = null;

                    Snackbar.make(binding.getRoot(),
                            "Tu cuenta no tiene una sucursal asignada. Contacta al administrador.",
                            Snackbar.LENGTH_LONG).show();

                    return;
                }

                if (listaSucursales.size() != 1) {
                    sucursalSeleccionada = null;

                    Snackbar.make(binding.getRoot(),
                            "Error en la asignación de sucursal. Contacta al administrador.",
                            Snackbar.LENGTH_LONG).show();

                    return;
                }

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

                sucursalSeleccionada = listaSucursales.get(0);

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
                        "Error obteniendo sucursales", throwable);

                if (isAdded() && binding != null) {
                    Snackbar.make(binding.getRoot(),
                            "No se pudieron cargar las sucursales",
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });
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
            Snackbar.make(binding.getRoot(),
                    "Seleccione una sucursal",
                    Snackbar.LENGTH_LONG).show();
            return;
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

// Enviar incidente y fotografías
        Call<RptaInsertarIncidente> call =
                jcPyAnyApi.insertarIncidente(
                        "JWT " + token,
                        campos,
                        partesFotos
                );


        call.enqueue(new Callback<RptaInsertarIncidente>() {
            @Override
            public void onResponse(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Response<RptaInsertarIncidente> response) {

                if (!isAdded() || binding == null) return;

                if (response.code() == 401) {
                    com.example.py_damb2026ii.util.SesionHelper.cerrarSesionPorVencimiento(requireContext());
                    return;
                }

                binding.addIncidentButton.setEnabled(true);
                binding.addIncidentButton.setText("Enviar incidente");

                if (!response.isSuccessful()) {

                    Snackbar.make(
                            binding.getRoot(),
                            "Error HTTP: " + response.code(),
                            Snackbar.LENGTH_LONG
                    ).show();

                    return;
                }

                RptaInsertarIncidente rpta = response.body();

                if (rpta != null && rpta.getCode() == 1) {

                    if (transaccionOK != null) {
                        transaccionOK.run();
                    }

                } else {

                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "No se recibió respuesta del servidor.";

                    Snackbar.make(
                            binding.getRoot(),
                            mensaje,
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                binding.addIncidentButton.setEnabled(true);
                binding.addIncidentButton.setText("Enviar incidente");

                Snackbar.make(
                        binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });
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

}
