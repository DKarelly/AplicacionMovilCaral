package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentAddCollaboratorBinding;
import com.example.py_damb2026ii.model.RequInsertarUsuario;
import com.example.py_damb2026ii.model.RptaInsertarUsuario;
import com.example.py_damb2026ii.model.RptaObtenerSucursales;
import com.example.py_damb2026ii.network.SucursalEntry;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddCollaboratorFragment extends Fragment {

    private FragmentAddCollaboratorBinding binding;

    private SharedPreferences sharedPref;

    private JCPyAnyApi api;

    private final List<SucursalEntry> sucursales =
            new ArrayList<>();

    private ArrayAdapter<SucursalEntry> branchAdapter;

    public AddCollaboratorFragment() {
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        sharedPref = requireActivity().getSharedPreferences(
                "USAT_SP",
                Context.MODE_PRIVATE
        );

        binding = FragmentAddCollaboratorBinding.inflate(
                inflater,
                container,
                false
        );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(JCPyAnyApi.class);

        ArrayAdapter<String> roleAdapter =
                new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        Arrays.asList("Técnico", "Solicitante")
                );

        roleAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        binding.spinnerRole.setAdapter(roleAdapter);

        binding.btnCreateBranch.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(
                        R.id.AddBranchFragment
                )
        );

        binding.btnSaveCollaborator.setOnClickListener(v ->
                registrarColaborador()
        );

        binding.btnCancelCollaborator.setOnClickListener(v ->
                Navigation.findNavController(v).popBackStack()
        );

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null) {
            cargarSucursales();
        }
    }

    private void cargarSucursales() {

        String token = sharedPref.getString(
                "TOKEN_JWT",
                ""
        );

        if (token.isEmpty()) return;

        binding.btnSaveCollaborator.setEnabled(false);

        api.obtenerSucursales("JWT " + token)
                .enqueue(new Callback<RptaObtenerSucursales>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RptaObtenerSucursales> call,
                            @NonNull Response<RptaObtenerSucursales> response) {

                        if (!isAdded() || binding == null) return;

                        if (response.code() == 401) {
                            com.example.py_damb2026ii.util.SesionHelper.cerrarSesionPorVencimiento(requireContext());
                            return;
                        }

                        RptaObtenerSucursales rpta = response.body();

                        if (!response.isSuccessful()
                                || rpta == null
                                || rpta.getCode() != 1) {

                            mostrarMensaje(
                                    "No se pudieron cargar las sucursales."
                            );

                            return;
                        }

                        sucursales.clear();

                        if (rpta.getData() != null) {
                            sucursales.addAll(rpta.getData());
                        }

                        branchAdapter = new ArrayAdapter<>(
                                requireContext(),
                                android.R.layout.simple_spinner_item,
                                sucursales
                        );

                        branchAdapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );

                        binding.spinnerBranch.setAdapter(branchAdapter);

                        boolean tieneSucursales = !sucursales.isEmpty();

                        binding.spinnerBranch.setEnabled(tieneSucursales);

                        binding.txtNoBranches.setVisibility(
                                tieneSucursales ? View.GONE : View.VISIBLE
                        );

                        binding.btnSaveCollaborator.setEnabled(
                                tieneSucursales
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RptaObtenerSucursales> call,
                            @NonNull Throwable t) {

                        if (!isAdded() || binding == null) return;

                        mostrarMensaje(
                                "Error de conexión al consultar sucursales."
                        );
                    }
                });
    }

    private void registrarColaborador() {

        binding.tilCollaboratorName.setError(null);
        binding.tilCollaboratorEmail.setError(null);
        binding.tilCollaboratorPassword.setError(null);

        String nombre = binding.etCollaboratorName
                .getText().toString().trim();

        String correo = binding.etCollaboratorEmail
                .getText().toString().trim();

        String password = binding.etCollaboratorPassword
                .getText().toString();

        String rol = binding.spinnerRole
                .getSelectedItem().toString();

        if (nombre.isEmpty()) {
            binding.tilCollaboratorName.setError(
                    "Ingresa el nombre completo"
            );
            return;
        }

        if (correo.isEmpty()
                || !Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {

            binding.tilCollaboratorEmail.setError(
                    "Ingresa un correo válido"
            );
            return;
        }

        if (password.length() < 8) {
            binding.tilCollaboratorPassword.setError(
                    "La contraseña debe tener al menos 8 caracteres"
            );
            return;
        }

        if (binding.spinnerBranch.getSelectedItem() == null) {
            mostrarMensaje(
                    "Debes seleccionar una sucursal."
            );
            return;
        }

        SucursalEntry sucursal =
                (SucursalEntry) binding.spinnerBranch.getSelectedItem();

        RequInsertarUsuario requ = new RequInsertarUsuario();

        requ.setNombre(nombre);
        requ.setCorreo(correo);
        requ.setPassword(password);
        requ.setRol(rol);
        requ.setId_sucursal(sucursal.id);

        String token = sharedPref.getString(
                "TOKEN_JWT",
                ""
        );

        binding.btnSaveCollaborator.setEnabled(false);

        api.insertarUsuario("JWT " + token, requ)
                .enqueue(new Callback<RptaInsertarUsuario>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RptaInsertarUsuario> call,
                            @NonNull Response<RptaInsertarUsuario> response) {

                        if (!isAdded() || binding == null) return;

                        if (response.code() == 401) {
                            com.example.py_damb2026ii.util.SesionHelper.cerrarSesionPorVencimiento(requireContext());
                            return;
                        }

                        binding.btnSaveCollaborator.setEnabled(true);

                        RptaInsertarUsuario rpta = response.body();

                        if (response.isSuccessful()
                                && rpta != null
                                && rpta.getCode() == 1) {

                            Snackbar.make(
                                    binding.getRoot(),
                                    "Colaborador registrado correctamente",
                                    Snackbar.LENGTH_LONG
                            ).show();

                            Navigation.findNavController(
                                    binding.getRoot()
                            ).popBackStack();

                        } else {

                            String mensaje = rpta != null
                                    ? rpta.getMsge()
                                    : "No se pudo registrar el colaborador.";

                            mostrarMensaje(mensaje);
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RptaInsertarUsuario> call,
                            @NonNull Throwable t) {

                        if (!isAdded() || binding == null) return;

                        binding.btnSaveCollaborator.setEnabled(true);

                        mostrarMensaje(
                                "Error de conexión al registrar colaborador."
                        );
                    }
                });
    }

    private void mostrarMensaje(String mensaje) {

        if (binding == null) return;

        Snackbar.make(
                binding.getRoot(),
                mensaje,
                Snackbar.LENGTH_LONG
        ).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}