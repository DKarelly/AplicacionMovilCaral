package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.py_damb2026ii.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentProfileBinding.inflate(
                inflater, container, false
        );

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);

        String nombre = prefs.getString("USER_NAME", "Usuario");
        String rol = prefs.getString("USER_ROLE", "Colaborador");

        binding.txtProfileName.setText(nombre);
        binding.txtProfileRole.setText(rol);

        binding.txtProfileNameDetail.setText(nombre);
        binding.txtProfileRoleDetail.setText(rol);

        binding.txtProfileInitials.setText(obtenerIniciales(nombre));

        binding.btnLogout.setOnClickListener(v ->
                ((MainActivity) requireActivity())
                        .solicitarCerrarSesion()
        );

        return binding.getRoot();
    }

    private String obtenerIniciales(String nombre) {

        if (nombre == null || nombre.trim().isEmpty()) {
            return "US";
        }

        String[] partes = nombre.trim().split("\\s+");

        if (partes.length >= 2) {
            return ("" + partes[0].charAt(0)
                    + partes[1].charAt(0)).toUpperCase();
        }

        return partes[0].substring(
                0, Math.min(2, partes[0].length())
        ).toUpperCase();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
