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

        binding.btnIrRegistrarSucursal.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.AddBranchFragment);
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
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        String token = sharedPref.getString("TOKEN_JWT", "");

        Call<RptaObtenerSucursales> call = jcPyAnyApi.obtenerSucursales("JWT " + token);
        call.enqueue(new Callback<RptaObtenerSucursales>() {
            @Override
            public void onResponse(@NonNull Call<RptaObtenerSucursales> call, @NonNull Response<RptaObtenerSucursales> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RptaObtenerSucursales rpta = response.body();
                    listaSucursales = rpta.getData();
                    if (listaSucursales != null && !listaSucursales.isEmpty()) {
                        ArrayAdapter<SucursalEntry> sucursalAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, listaSucursales);
                        sucursalAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.sucursalSpinner.setAdapter(sucursalAdapter);

                        binding.sucursalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                sucursalSeleccionada = listaSucursales.get(position);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                sucursalSeleccionada = null;
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaObtenerSucursales> call, @NonNull Throwable throwable) {
                Log.d("USAT_APP", "Error cargando sucursales: " + throwable.getMessage());
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

        RequInsertarIncidente requ = new RequInsertarIncidente();
        requ.setTitulo(binding.tituloEditText.getText() != null ? binding.tituloEditText.getText().toString() : "");
        requ.setDescripcion(binding.descripcionEditText.getText() != null ? binding.descripcionEditText.getText().toString() : "");

        // Categoría
        String categoriaSelected = binding.categoriaSpinner.getSelectedItem() != null ? binding.categoriaSpinner.getSelectedItem().toString() : "Otro";
        if ("Otro".equals(categoriaSelected)) {
            categoriaSelected = binding.categoriaCustomEdit.getText() != null ? binding.categoriaCustomEdit.getText().toString() : "Otro";
        }
        requ.setCategoria(categoriaSelected);

        // Prioridad (limpiando emojis de semáforo si es necesario, o enviando Alta, Media, Baja)
        String prioridadRaw = binding.prioridadSpinner.getSelectedItem() != null ? binding.prioridadSpinner.getSelectedItem().toString() : "🟡 Media";
        String prioridadClean = "Media";
        if (prioridadRaw.contains("Alta")) prioridadClean = "Alta";
        else if (prioridadRaw.contains("Media")) prioridadClean = "Media";
        else if (prioridadRaw.contains("Baja")) prioridadClean = "Baja";
        requ.setPrioridad(prioridadClean);

        // Sucursal ID
        int idSucursal = sucursalSeleccionada != null ? sucursalSeleccionada.id : 1;
        requ.setId_sucursal(idSucursal);

        Call<RptaInsertarIncidente> call = jcPyAnyApi.insertarIncidente("JWT " + token, requ);
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
