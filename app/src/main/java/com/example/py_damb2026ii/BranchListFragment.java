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
import com.example.py_damb2026ii.databinding.FragmentBranchListBinding;
import com.example.py_damb2026ii.model.RptaObtenerSucursales;
import com.example.py_damb2026ii.network.SucursalEntry;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BranchListFragment extends Fragment {

    private FragmentBranchListBinding binding;
    private SharedPreferences sharedPref;
    private List<SucursalEntry> listaSucursales;

    public BranchListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentBranchListBinding.inflate(inflater, container, false);

        obtenerSucursalesApi(() -> {
            RecyclerView recyclerView = binding.recyclerViewBranches;
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            BranchCardRecyclerViewAdapter adapter = new BranchCardRecyclerViewAdapter(listaSucursales);
            recyclerView.setAdapter(adapter);
        });

        return binding.getRoot();
    }

    private void obtenerSucursalesApi(Runnable listaCargada) {
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
                if (!response.isSuccessful()) {
                    Log.d("USAT_APP", "Código error: " + response.code());
                } else {
                    RptaObtenerSucursales rpta = response.body();
                    if (rpta != null) {
                        listaSucursales = rpta.getData();
                        if (listaCargada != null) {
                            listaCargada.run();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaObtenerSucursales> call, @NonNull Throwable throwable) {
                if (throwable.getMessage() != null) {
                    Log.d("USAT_APP", throwable.getMessage());
                }
            }
        });
    }
}
