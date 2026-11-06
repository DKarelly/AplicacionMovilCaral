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

import com.example.py_damb2026ii.databinding.FragmentSolicitanteHomeBinding;
import com.google.android.material.snackbar.Snackbar;

public class SolicitanteHomeFragment extends Fragment {

    private FragmentSolicitanteHomeBinding binding;
    private SharedPreferences sharedPref;

    public SolicitanteHomeFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        binding = FragmentSolicitanteHomeBinding.inflate(inflater, container, false);

        binding.txtWelcomeUser.setText("IncidentApp");

        binding.cardReportarIncidente.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.AddIncidentFragment)
        );

        binding.cardMisIncidentes.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.IncidentListFragment)
        );

        binding.cardNotificaciones.setOnClickListener(v ->
                Snackbar.make(v, "No hay nuevas alertas", Snackbar.LENGTH_SHORT).show()
        );

        binding.cardMiPerfil.setOnClickListener(v ->
                Snackbar.make(v, "Perfil disponible próximamente", Snackbar.LENGTH_SHORT).show()
        );

        return binding.getRoot();
    }
}