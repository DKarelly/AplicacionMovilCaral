package com.example.py_damb2026ii;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.py_damb2026ii.databinding.FragmentTecnicoHomeBinding;
import com.google.android.material.snackbar.Snackbar;

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

        binding.cardEscanearQr.setOnClickListener(v ->
                Snackbar.make(v, "Escaneo QR: próximamente", Snackbar.LENGTH_SHORT).show()
        );

        binding.cardMapaUbicacion.setOnClickListener(v ->
                Snackbar.make(v, "Mapa: próximamente", Snackbar.LENGTH_SHORT).show()
        );

        binding.cardTecnicoPerfil.setOnClickListener(v ->
                Snackbar.make(v, "Perfil técnico: próximamente", Snackbar.LENGTH_SHORT).show()
        );

        return binding.getRoot();
    }
}