package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentTecnicoHomeBinding;
import com.example.py_damb2026ii.model.RptaObtenerActivoQR;
import com.example.py_damb2026ii.util.QrHelper;
import com.example.py_damb2026ii.util.SesionHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TecnicoHomeFragment extends Fragment {

    private FragmentTecnicoHomeBinding binding;

    public TecnicoHomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentTecnicoHomeBinding.inflate(inflater, container, false);

        binding.cardIncidentesAsignados.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.IncidentListFragment)
        );

        binding.cardEscanearQr.setOnClickListener(v -> {
            QrHelper.escanearQr(requireContext(), new QrHelper.OnQrScannedListener() {
                @Override
                public void onScanned(String rawValue) {
                    consultarActivoTecnico(rawValue);
                }
                @Override public void onError(Exception e) {}
                @Override public void onCancelled() {}
            });
        });

        binding.cardMapaUbicacion.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.MapFragment)
        );

        binding.cardTecnicoPerfil.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.ProfileFragment)
        );

        return binding.getRoot();
    }

    private void consultarActivoTecnico(String codigoQR) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_JWT", "");

        JCPyAnyApi api = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JCPyAnyApi.class);

        api.obtenerActivoQR("JWT " + token, codigoQR).enqueue(new Callback<RptaObtenerActivoQR>() {
            @Override
            public void onResponse(@NonNull Call<RptaObtenerActivoQR> call, @NonNull Response<RptaObtenerActivoQR> response) {
                if (!isAdded() || binding == null) return;
                if (response.code() == 401) {
                    SesionHelper.cerrarSesionPorVencimiento(requireContext());
                    return;
                }
                if (!response.isSuccessful() || response.body() == null) {
                    Snackbar.make(binding.getRoot(), "Activo no encontrado o sin acceso", Snackbar.LENGTH_LONG).show();
                    return;
                }
                RptaObtenerActivoQR rpta = response.body();
                if (rpta.getCode() == 1 && rpta.getData() != null) {
                    RptaObtenerActivoQR.ActivoQR activo = rpta.getData();
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Información del Activo")
                            .setMessage("Nombre: " + activo.nombre + "\nTipo: " + activo.tipo + "\nEstado: " + activo.estado + "\nSucursal: " + activo.nombre_sucursal)
                            .setPositiveButton("Aceptar", null)
                            .show();
                } else {
                    Snackbar.make(binding.getRoot(), rpta.getMsge() != null ? rpta.getMsge() : "Activo no encontrado", Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaObtenerActivoQR> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) return;
                Snackbar.make(binding.getRoot(), "Error de conexión", Snackbar.LENGTH_SHORT).show();
            }
        });
    }
}
