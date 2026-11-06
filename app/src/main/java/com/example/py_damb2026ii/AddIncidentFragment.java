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

public class AddIncidentFragment extends Fragment {

    private FragmentAddIncidentBinding binding;
    private SharedPreferences sharedPref;
    private List<SucursalEntry> listaSucursales;
    private SucursalEntry sucursalSeleccionada;

    public AddIncidentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentAddIncidentBinding.inflate(inflater, container, false);

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

                Log.d("USAT_APP", "HTTP sucursales: " + response.code());

                if (!isAdded() || binding == null) return;

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


// Enviar al servidor
        Call<RptaInsertarIncidente> call =
                jcPyAnyApi.insertarIncidente(
                        "JWT " + token,
                        campos,
                        Collections.emptyList()
                );

        call.enqueue(new Callback<RptaInsertarIncidente>() {
            @Override
            public void onResponse(@NonNull Call<RptaInsertarIncidente> call, @NonNull Response<RptaInsertarIncidente> response) {
                if (!response.isSuccessful()) {
                    Log.d("USAT_APP", "Código error: " + response.code());
                } else {
                    RptaInsertarIncidente rpta = response.body();
                    if (rpta != null && rpta.getCode() == 1) {
                        if (transaccionOK != null) {
                            transaccionOK.run();
                        }
                    } else if (rpta != null) {
                        Log.d("USAT_APP", "Mensaje: " + rpta.getMsge());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaInsertarIncidente> call, @NonNull Throwable throwable) {
                if (throwable.getMessage() != null) {
                    Log.d("USAT_APP", throwable.getMessage());
                }
            }
        });
    }
}
