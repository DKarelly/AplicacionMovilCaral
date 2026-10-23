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

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentAddIncidentBinding;
import com.example.py_damb2026ii.model.RequInsertarIncidente;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddIncidentFragment extends Fragment {

    private FragmentAddIncidentBinding binding;
    private SharedPreferences sharedPref;

    public AddIncidentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentAddIncidentBinding.inflate(inflater, container, false);

        binding.addIncidentButton.setOnClickListener(v -> insertarIncidente(() -> {
            Snackbar.make(binding.getRoot(), "Incidente registrado correctamente", Snackbar.LENGTH_LONG)
                    .setAnchorView(requireActivity().findViewById(R.id.fab))
                    .setAction("Action", null).show();
            Navigation.findNavController(v).navigate(R.id.IncidentListFragment);
        }));

        binding.cancelAddIncidentButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return binding.getRoot();
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
        requ.setCategoria(binding.categoriaEditText.getText() != null ? binding.categoriaEditText.getText().toString() : "");
        requ.setPrioridad(binding.prioridadEditText.getText() != null ? binding.prioridadEditText.getText().toString() : "");
        
        String sucursalStr = binding.sucursalEditText.getText() != null ? binding.sucursalEditText.getText().toString() : "1";
        int idSucursal;
        try {
            idSucursal = Integer.parseInt(sucursalStr);
        } catch (NumberFormatException e) {
            idSucursal = 1;
        }
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
