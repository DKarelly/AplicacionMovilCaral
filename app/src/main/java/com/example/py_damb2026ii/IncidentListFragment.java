package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentIncidentListBinding.inflate(inflater, container, false);

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
            public void onResponse(Call<RptaObtenerIncidentes> call, Response<RptaObtenerIncidentes> response) {
                if (!response.isSuccessful()) {
                    Log.d("USAT_APP", "Código error: " + response.code());
                } else {
                    RptaObtenerIncidentes rpta = response.body();
                    if (rpta != null) {
                        listaIncidentes = rpta.getData();
                        if (listaCargada != null) {
                            listaCargada.run();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<RptaObtenerIncidentes> call, Throwable throwable) {
                Log.d("USAT_APP", throwable.getMessage());
            }
        });
    }
}
