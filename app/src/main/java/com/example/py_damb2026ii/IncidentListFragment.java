package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentIncidentListBinding;
import com.example.py_damb2026ii.model.RptaObtenerIncidentes;
import com.example.py_damb2026ii.network.IncidentEntry;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class IncidentListFragment extends Fragment {

    private FragmentIncidentListBinding binding;
    private SharedPreferences sharedPref;
    private List<IncidentEntry> listaIncidentes;

    public IncidentListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentIncidentListBinding.inflate(inflater, container, false);

        String userRole = sharedPref.getString("USER_ROLE", "Solicitante");
        if ("Administrador".equalsIgnoreCase(userRole)) {
            binding.adminQuickAccessLayout.setVisibility(View.VISIBLE);
        } else {
            binding.adminQuickAccessLayout.setVisibility(View.GONE);
        }

        binding.btnQuickIncidentes.setOnClickListener(v -> 
            Snackbar.make(v, "Acceso Rápido: Todos los Incidentes", Snackbar.LENGTH_SHORT).show()
        );
        binding.btnQuickTecnicos.setOnClickListener(v -> 
            Snackbar.make(v, "Acceso Rápido: Gestión de Técnicos", Snackbar.LENGTH_SHORT).show()
        );
        binding.btnQuickReportes.setOnClickListener(v -> 
            Snackbar.make(v, "Acceso Rápido: Reportes y Métricas", Snackbar.LENGTH_SHORT).show()
        );

        obtenerIncidentesApi(() -> {
            RecyclerView recyclerView = binding.recyclerViewIncidents;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            IncidentCardRecyclerViewAdapter adapter = new IncidentCardRecyclerViewAdapter(listaIncidentes);
            recyclerView.setAdapter(adapter);
        });

        return binding.getRoot();
    }

    private void obtenerIncidentesApi(Runnable listaCargada) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        String token = sharedPref.getString("TOKEN_JWT", "");
        Call<RptaObtenerIncidentes> call = jcPyAnyApi.obtenerIncidentes("JWT " + token);
        call.enqueue(new Callback<RptaObtenerIncidentes>() {
            @Override
            public void onResponse(@NonNull Call<RptaObtenerIncidentes> call, @NonNull Response<RptaObtenerIncidentes> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null || response.body().getData().isEmpty()) {
                    Log.d("USAT_APP", "API falló o vacía, cargando incidentes simulados desde JSON local.");
                    cargarIncidentesLocales(listaCargada);
                } else {
                    RptaObtenerIncidentes rpta = response.body();
                    listaIncidentes = rpta.getData();
                    if (listaCargada != null) {
                        listaCargada.run();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaObtenerIncidentes> call, @NonNull Throwable throwable) {
                Log.d("USAT_APP", "Fallo de red: " + throwable.getMessage() + ". Cargando incidentes simulados locales.");
                cargarIncidentesLocales(listaCargada);
            }
        });
    }

    private void cargarIncidentesLocales(Runnable listaCargada) {
        try {
            InputStream inputStream = getResources().openRawResource(R.raw.incidentes);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Gson gson = new Gson();
            Type listType = new TypeToken<List<IncidentEntry>>() {}.getType();
            listaIncidentes = gson.fromJson(reader, listType);
            if (listaCargada != null) {
                listaCargada.run();
            }
        } catch (Exception e) {
            Log.d("USAT_APP", "Error leyendo JSON local de incidentes: " + e.getMessage());
        }
    }
}
