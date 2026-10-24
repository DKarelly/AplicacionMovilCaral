package com.example.py_damb2026ii;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.py_damb2026ii.databinding.FragmentIncidentConfirmationBinding;

public class IncidentConfirmationFragment extends Fragment {

    private FragmentIncidentConfirmationBinding binding;

    public IncidentConfirmationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentIncidentConfirmationBinding.inflate(inflater, container, false);

        binding.btnVerMisIncidentes.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.IncidentListFragment)
        );

        binding.btnVolverInicio.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.SolicitanteHomeFragment)
        );

        return binding.getRoot();
    }
}
