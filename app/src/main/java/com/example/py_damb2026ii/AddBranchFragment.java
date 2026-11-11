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

        binding.addBranchButton.setOnClickListener(v -> insertarSucursal());

        binding.cancelAddBranchButton.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        return binding.getRoot();
    }


    private void insertarSucursal() {

        binding.nombreSucursalInput.setError(null);
        binding.direccionSucursalInput.setError(null);
        binding.latitudSucursalInput.setError(null);
        binding.longitudSucursalInput.setError(null);

        String nombre = binding.nombreSucursalEdit.getText().toString().trim();
        String direccion = binding.direccionSucursalEdit.getText().toString().trim();
        String latitudTexto = binding.latitudSucursalEdit.getText().toString().trim();
        String longitudTexto = binding.longitudSucursalEdit.getText().toString().trim();

        if (nombre.isEmpty()) {
            binding.nombreSucursalInput.setError("Ingrese el nombre");
            return;
        }

        if (direccion.isEmpty()) {
            binding.direccionSucursalInput.setError("Ingrese la dirección");
            return;
        }

        double latitud;
        double longitud;

        try {
            if (latitudTexto.isEmpty() || longitudTexto.isEmpty()) {
                throw new NumberFormatException();
            }

            latitud = Double.parseDouble(latitudTexto);
            longitud = Double.parseDouble(longitudTexto);

            if (latitud < -90 || latitud > 90) {
                binding.latitudSucursalInput.setError("Latitud no válida");
                return;
            }

            if (longitud < -180 || longitud > 180) {
                binding.longitudSucursalInput.setError("Longitud no válida");
                return;
            }

        } catch (NumberFormatException e) {

            binding.latitudSucursalInput.setError(
                    "Ingrese coordenadas numéricas válidas"
            );

            return;
        }

        String token = sharedPref.getString("TOKEN_JWT", "");

        if (token.isEmpty()) {
            Snackbar.make(binding.getRoot(),
                    "La sesión ha expirado",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        RequInsertarSucursal requ = new RequInsertarSucursal();

        requ.setNombre(nombre);
        requ.setDireccion(direccion);
        requ.setLatitud(latitud);
        requ.setLongitud(longitud);

        // NO enviar id_empresa.
        // Flask obtiene la empresa desde el usuario autenticado.

        binding.addBranchButton.setEnabled(false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        Call<RptaInsertarSucursal> call =
                api.insertarSucursal("JWT " + token, requ);

        call.enqueue(new Callback<RptaInsertarSucursal>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaInsertarSucursal> call,
                    @NonNull Response<RptaInsertarSucursal> response) {

                if (!isAdded() || binding == null) return;

                if (response.code() == 401) {
                    com.example.py_damb2026ii.util.SesionHelper.cerrarSesionPorVencimiento(requireContext());
                    return;
                }

                binding.addBranchButton.setEnabled(true);

                RptaInsertarSucursal rpta = response.body();

                if (response.isSuccessful()
                        && rpta != null
                        && rpta.getCode() == 1) {

                    Snackbar.make(
                            binding.getRoot(),
                            "Sucursal registrada correctamente",
                            Snackbar.LENGTH_LONG
                    ).show();

                    Navigation.findNavController(
                            binding.getRoot()
                    ).popBackStack();

                } else {

                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "No se pudo registrar la sucursal. Código HTTP: "
                              + response.code();

                    Snackbar.make(
                            binding.getRoot(),
                            mensaje,
                            Snackbar.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaInsertarSucursal> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                binding.addBranchButton.setEnabled(true);

                Snackbar.make(
                        binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG
                ).show();
            }
        });
    }

}
