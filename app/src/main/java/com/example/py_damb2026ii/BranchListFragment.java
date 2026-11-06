package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentBranchListBinding;
import com.example.py_damb2026ii.model.RptaObtenerSucursales;
import com.example.py_damb2026ii.model.RptaObtenerUsuarios;
import com.example.py_damb2026ii.network.SucursalEntry;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BranchListFragment extends Fragment {

    private FragmentBranchListBinding binding;

    private SharedPreferences sharedPref;

    private JCPyAnyApi api;

    private final List<SucursalEntry> sucursales =
            new ArrayList<>();

    private final List<RptaObtenerUsuarios.Usuario> colaboradores =
            new ArrayList<>();

    private boolean mostrandoSucursales = true;

    public BranchListFragment() {
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

        binding = FragmentBranchListBinding.inflate(
                inflater,
                container,
                false
        );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(JCPyAnyApi.class);

        binding.recyclerViewBranches.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        binding.recyclerViewCollaborators.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        configurarEventos();

        mostrarPestana(true);

        return binding.getRoot();
    }

    private boolean esAdministradorEmpresa() {

        return "Administrador".equalsIgnoreCase(
                sharedPref.getString("USER_ROLE", "")
        );
    }

    private void configurarEventos() {

        binding.btnTabBranches.setOnClickListener(v ->
                mostrarPestana(true)
        );

        binding.btnTabCollaborators.setOnClickListener(v ->
                mostrarPestana(false)
        );

        binding.btnAddBranch.setOnClickListener(v -> {

            Navigation.findNavController(v).navigate(
                    R.id.AddBranchFragment
            );
        });

        binding.btnAddCollaborator.setOnClickListener(v -> {

            Navigation.findNavController(v).navigate(
                    R.id.AddCollaboratorFragment
            );
        });

        binding.inputSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count) {

                        if (mostrandoSucursales) {
                            renderizarSucursales();
                        } else {
                            renderizarColaboradores();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                }
        );
    }

    private void mostrarPestana(boolean sucursalesTab) {

        if (binding == null) return;

        mostrandoSucursales = sucursalesTab;

        binding.recyclerViewBranches.setVisibility(
                sucursalesTab ? View.VISIBLE : View.GONE
        );

        binding.recyclerViewCollaborators.setVisibility(
                sucursalesTab ? View.GONE : View.VISIBLE
        );

        binding.btnAddBranch.setVisibility(
                sucursalesTab ? View.VISIBLE : View.GONE
        );

        binding.btnAddCollaborator.setVisibility(
                sucursalesTab ? View.GONE : View.VISIBLE
        );

        binding.txtEmptyBranches.setVisibility(View.GONE);
        binding.txtEmptyCollaborators.setVisibility(View.GONE);

        binding.inputSearch.setHint(
                sucursalesTab
                        ? "Buscar sucursal..."
                        : "Buscar colaborador..."
        );

        actualizarEstiloTab(
                binding.btnTabBranches,
                sucursalesTab
        );

        actualizarEstiloTab(
                binding.btnTabCollaborators,
                !sucursalesTab
        );

        if (sucursalesTab) {
            renderizarSucursales();
        } else {
            renderizarColaboradores();
        }
    }

    private void actualizarEstiloTab(
            MaterialButton boton,
            boolean seleccionado) {

        int fondo = Color.parseColor(
                seleccionado ? "#E7F2F8" : "#FFFFFF"
        );

        int texto = Color.parseColor(
                seleccionado ? "#4E7D9A" : "#71869A"
        );

        int borde = Color.parseColor("#D9E6EE");

        boton.setBackgroundTintList(
                ColorStateList.valueOf(fondo)
        );

        boton.setTextColor(texto);

        boton.setStrokeColor(
                ColorStateList.valueOf(borde)
        );
    }

    @Override
    public void onResume() {
        super.onResume();

        if (binding != null && esAdministradorEmpresa()) {
            cargarSucursales();
            cargarColaboradores();
        }
    }

    private void cargarSucursales() {

        String token = sharedPref.getString(
                "TOKEN_JWT",
                ""
        );

        if (token.isEmpty()) return;

        if (mostrandoSucursales) {
            mostrarEstado(
                    "Cargando sucursales...",
                    true
            );
        }

        api.obtenerSucursales("JWT " + token)
                .enqueue(new Callback<RptaObtenerSucursales>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RptaObtenerSucursales> call,
                            @NonNull Response<RptaObtenerSucursales> response) {

                        if (!isAdded() || binding == null) return;

                        RptaObtenerSucursales rpta = response.body();

                        if (!response.isSuccessful()
                                || rpta == null
                                || rpta.getCode() != 1) {

                            mostrarEstado(
                                    "No se pudieron cargar las sucursales.",
                                    true
                            );

                            return;
                        }

                        sucursales.clear();

                        if (rpta.getData() != null) {
                            sucursales.addAll(rpta.getData());
                        }

                        renderizarSucursales();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RptaObtenerSucursales> call,
                            @NonNull Throwable t) {

                        if (!isAdded() || binding == null) return;

                        mostrarEstado(
                                "Error de conexión al cargar sucursales.",
                                true
                        );
                    }
                });
    }

    private void cargarColaboradores() {

        String token = sharedPref.getString(
                "TOKEN_JWT",
                ""
        );

        if (token.isEmpty()) return;

        if (!mostrandoSucursales) {
            mostrarEstado(
                    "Cargando colaboradores...",
                    false
            );
        }

        api.obtenerUsuarios("JWT " + token)
                .enqueue(new Callback<RptaObtenerUsuarios>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RptaObtenerUsuarios> call,
                            @NonNull Response<RptaObtenerUsuarios> response) {

                        if (!isAdded() || binding == null) return;

                        RptaObtenerUsuarios rpta = response.body();

                        if (!response.isSuccessful()
                                || rpta == null
                                || rpta.getCode() != 1) {

                            mostrarEstado(
                                    "No se pudieron cargar los colaboradores.",
                                    false
                            );

                            return;
                        }

                        colaboradores.clear();

                        if (rpta.getData() != null) {

                            for (RptaObtenerUsuarios.Usuario usuario
                                    : rpta.getData()) {

                                String rol = usuario.getRol();

                                if ("Técnico".equalsIgnoreCase(rol)
                                        || "Solicitante".equalsIgnoreCase(rol)) {

                                    colaboradores.add(usuario);
                                }
                            }
                        }

                        renderizarColaboradores();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RptaObtenerUsuarios> call,
                            @NonNull Throwable t) {

                        if (!isAdded() || binding == null) return;

                        mostrarEstado(
                                "Error de conexión al cargar colaboradores.",
                                false
                        );
                    }
                });
    }

    private void renderizarSucursales() {

        if (binding == null || !mostrandoSucursales) return;

        String busqueda = binding.inputSearch
                .getText().toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        List<SucursalEntry> filtradas = new ArrayList<>();

        for (SucursalEntry sucursal : sucursales) {

            String nombre = sucursal.nombre == null
                    ? ""
                    : sucursal.nombre.toLowerCase(Locale.ROOT);

            String direccion = sucursal.direccion == null
                    ? ""
                    : sucursal.direccion.toLowerCase(Locale.ROOT);

            if (nombre.contains(busqueda)
                    || direccion.contains(busqueda)) {

                filtradas.add(sucursal);
            }
        }

        if (filtradas.isEmpty()) {

            mostrarEstado(
                    sucursales.isEmpty()
                            ? "Todavía no tienes sucursales registradas."
                            : "No se encontraron sucursales.",
                    true
            );

            return;
        }

        binding.txtEmptyBranches.setVisibility(View.GONE);
        binding.recyclerViewBranches.setVisibility(View.VISIBLE);

        BranchCardRecyclerViewAdapter adapter =
                new BranchCardRecyclerViewAdapter(filtradas);

        binding.recyclerViewBranches.setAdapter(adapter);
    }

    private void renderizarColaboradores() {

        if (binding == null || mostrandoSucursales) return;

        String busqueda = binding.inputSearch
                .getText().toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        List<RptaObtenerUsuarios.Usuario> filtrados =
                new ArrayList<>();

        for (RptaObtenerUsuarios.Usuario usuario : colaboradores) {

            String nombre = usuario.getNombre() == null
                    ? ""
                    : usuario.getNombre().toLowerCase(Locale.ROOT);

            String correo = usuario.getCorreo() == null
                    ? ""
                    : usuario.getCorreo().toLowerCase(Locale.ROOT);

            String sucursal = usuario.getNombreSucursal() == null
                    ? ""
                    : usuario.getNombreSucursal().toLowerCase(Locale.ROOT);

            if (nombre.contains(busqueda)
                    || correo.contains(busqueda)
                    || sucursal.contains(busqueda)) {

                filtrados.add(usuario);
            }
        }

        if (filtrados.isEmpty()) {

            mostrarEstado(
                    colaboradores.isEmpty()
                            ? "Todavía no tienes colaboradores registrados."
                            : "No se encontraron colaboradores.",
                    false
            );

            return;
        }

        binding.txtEmptyCollaborators.setVisibility(View.GONE);
        binding.recyclerViewCollaborators.setVisibility(View.VISIBLE);

        CollaboratorCardRecyclerViewAdapter adapter =
                new CollaboratorCardRecyclerViewAdapter(filtrados);

        binding.recyclerViewCollaborators.setAdapter(adapter);
    }

    private void mostrarEstado(
            String mensaje,
            boolean esSucursal) {

        if (binding == null) return;

        if (esSucursal) {

            binding.recyclerViewBranches.setVisibility(View.GONE);

            binding.txtEmptyBranches.setText(mensaje);
            binding.txtEmptyBranches.setVisibility(View.VISIBLE);

        } else {

            binding.recyclerViewCollaborators.setVisibility(View.GONE);

            binding.txtEmptyCollaborators.setText(mensaje);
            binding.txtEmptyCollaborators.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}