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
import com.example.py_damb2026ii.databinding.FragmentAddBranchBinding;
import com.example.py_damb2026ii.model.RequInsertarSucursal;
import com.example.py_damb2026ii.model.RptaInsertarSucursal;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddBranchFragment extends Fragment {

    private FragmentAddBranchBinding binding;
    private SharedPreferences sharedPref;

    public AddBranchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentAddBranchBinding.inflate(inflater, container, false);

        binding.addBranchButton.setOnClickListener(v -> insertarSucursal(() -> {
            Snackbar.make(binding.getRoot(), "Sucursal registrada correctamente", Snackbar.LENGTH_LONG)
                    .setAnchorView(requireActivity().findViewById(R.id.fab))
                    .setAction("Action", null).show();
            Navigation.findNavController(v).popBackStack();
        }));

        binding.cancelAddBranchButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return binding.getRoot();
    }

    private void insertarSucursal(Runnable transaccionOK) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        String token = sharedPref.getString("TOKEN_JWT", "");

        RequInsertarSucursal requ = new RequInsertarSucursal();
        requ.setNombre(binding.nombreSucursalEdit.getText() != null ? binding.nombreSucursalEdit.getText().toString() : "");
        requ.setDireccion(binding.direccionSucursalEdit.getText() != null ? binding.direccionSucursalEdit.getText().toString() : "");

        double lat = 0.0;
        try {
            String latStr = binding.latitudSucursalEdit.getText() != null ? binding.latitudSucursalEdit.getText().toString() : "0";
            lat = Double.parseDouble(latStr);
        } catch (NumberFormatException ignored) {}
        requ.setLatitud(lat);

        double lon = 0.0;
        try {
            String lonStr = binding.longitudSucursalEdit.getText() != null ? binding.longitudSucursalEdit.getText().toString() : "0";
            lon = Double.parseDouble(lonStr);
        } catch (NumberFormatException ignored) {}
        requ.setLongitud(lon);

        requ.setId_empresa(1);

        Call<RptaInsertarSucursal> call = jcPyAnyApi.insertarSucursal("JWT " + token, requ);
        call.enqueue(new Callback<RptaInsertarSucursal>() {
            @Override
            public void onResponse(@NonNull Call<RptaInsertarSucursal> call, @NonNull Response<RptaInsertarSucursal> response) {
                if (!response.isSuccessful()) {
                    Log.d("USAT_APP", "Código error: " + response.code());
                } else {
                    RptaInsertarSucursal rpta = response.body();
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
            public void onFailure(@NonNull Call<RptaInsertarSucursal> call, @NonNull Throwable throwable) {
                if (throwable.getMessage() != null) {
                    Log.d("USAT_APP", throwable.getMessage());
                }
            }
        });
    }
}
