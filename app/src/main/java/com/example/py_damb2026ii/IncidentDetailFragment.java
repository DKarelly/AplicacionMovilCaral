
package com.example.py_damb2026ii;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentIncidentDetailBinding;
import com.example.py_damb2026ii.model.RptaDetalleIncidente;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.py_damb2026ii.model.RptaObtenerUsuarios;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;

import com.example.py_damb2026ii.model.RptaInsertarIncidente;

public class IncidentDetailFragment extends Fragment {

    private FragmentIncidentDetailBinding binding;
    private SharedPreferences sharedPref;
    private int idIncidenteActual = -1;
    private String estadoActual = "";

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentIncidentDetailBinding.inflate(
                inflater, container, false);

        sharedPref = requireActivity().getSharedPreferences(
                "USAT_SP", Context.MODE_PRIVATE);

        int idIncidente = -1;

        if (getArguments() != null) {
            idIncidente = getArguments()
                    .getInt("id_incidente", -1);
        }

        idIncidenteActual = idIncidente;

        binding.btnCambiarEstado.setOnClickListener(
                v -> mostrarDialogoCambioEstado()
        );

        binding.btnAsignarTecnico.setOnClickListener(
                v -> cargarTecnicos()
        );


        if (idIncidente > 0) {
            binding.detailTitle.setText("Cargando incidente...");
            cargarDetalle(idIncidente);
        } else {
            binding.detailTitle.setText(
                    "No se seleccionó un incidente");
        }

        return binding.getRoot();
    }

    private void cargarDetalle(int idIncidente) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");

        Call<RptaDetalleIncidente> call =
                api.obtenerDetalleIncidente(
                        "JWT " + token, idIncidente);

        call.enqueue(new Callback<RptaDetalleIncidente>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaDetalleIncidente> call,
                    @NonNull Response<RptaDetalleIncidente> response) {

                if (!isAdded() || binding == null) return;

                if (!response.isSuccessful()
                        || response.body() == null) {

                    mostrarError("Error HTTP: " + response.code());
                    return;
                }

                RptaDetalleIncidente rpta = response.body();

                if (rpta.getCode() != 1
                        || rpta.getData() == null
                        || rpta.getData().incidente == null) {

                    mostrarError(rpta.getMsge());
                    return;
                }

                mostrarIncidente(rpta.getData().incidente);
                mostrarHistorial(rpta.getData().historial);
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaDetalleIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                mostrarError("Error de conexión: "
                        + throwable.getMessage());
            }
        });
    }

    private void mostrarIncidente(
            RptaDetalleIncidente.Incidente incidente) {

        binding.detailCode.setText(
                String.format(Locale.getDefault(),
                        "#INC-%03d", incidente.id));

        binding.detailTitle.setText(incidente.titulo);
        binding.detailPriority.setText(incidente.prioridad);
        binding.detailStatus.setText(incidente.estado);

        binding.detailSucursal.setText(
                "Sucursal: " + incidente.sucursal);

// Mostrar técnico asignado
        String nombreTecnico = incidente.tecnico;

        if (nombreTecnico == null || nombreTecnico.isEmpty()) {
            binding.detailTecnico.setText("Técnico: Sin asignar");
        } else {
            binding.detailTecnico.setText(
                    "Técnico: " + nombreTecnico);
        }

// Mostrar asignación solo al Administrador
        String rolUsuario = sharedPref.getString("USER_ROLE", "");

        binding.btnAsignarTecnico.setVisibility(
                "Administrador".equalsIgnoreCase(rolUsuario)
                        ? View.VISIBLE
                        : View.GONE
        );


        if (incidente.id_activo != null) {
            binding.detailActivo.setText(
                    "ID del activo: " + incidente.id_activo);
        } else {
            binding.detailActivo.setText(
                    "Activo: No asociado");
        }

        binding.detailFecha.setText(
                "Fecha: " + incidente.fecha_registro);

        binding.detailDescription.setText(
                incidente.descripcion);

        // Color de la prioridad
        int color = Color.parseColor("#F57C00");

        if ("Alta".equals(incidente.prioridad)) {
            color = Color.parseColor("#D32F2F");
        } else if ("Baja".equals(incidente.prioridad)) {
            color = Color.parseColor("#388E3C");
        }

        binding.detailPriority.setBackgroundColor(color);

        estadoActual = incidente.estado;

        String rol = sharedPref.getString("USER_ROLE", "");

        boolean puedeCambiar =
                "Técnico".equalsIgnoreCase(rol)
                        && ("Abierto".equalsIgnoreCase(estadoActual)
                        || "En proceso".equalsIgnoreCase(estadoActual));

        binding.btnCambiarEstado.setVisibility(
                puedeCambiar ? View.VISIBLE : View.GONE
        );

    }

    private void mostrarHistorial(
            List<RptaDetalleIncidente.CambioEstado> historial) {

        LinearLayout contenedor =
                binding.detailHistorialContainer;

        contenedor.removeAllViews();

        if (historial == null || historial.isEmpty()) {
            TextView vacio = new TextView(requireContext());
            vacio.setText("No hay cambios registrados");
            contenedor.addView(vacio);
            return;
        }

        for (RptaDetalleIncidente.CambioEstado cambio : historial) {

            TextView estado = new TextView(requireContext());

            String movimiento;

            if (cambio.estado_anterior == null) {
                movimiento = "Registro inicial: "
                        + cambio.estado_nuevo;
            } else {
                movimiento = cambio.estado_anterior
                        + " → " + cambio.estado_nuevo;
            }

            estado.setText(movimiento);
            estado.setTextSize(15);
            estado.setTextColor(Color.BLACK);
            estado.setTypeface(null, Typeface.BOLD);
            estado.setPadding(0, 18, 0, 4);

            contenedor.addView(estado);

            TextView usuarioFecha =
                    new TextView(requireContext());

            usuarioFecha.setText(
                    "Por: " + cambio.usuario
                            + "\nFecha: " + cambio.fecha_cambio);

            usuarioFecha.setTextSize(12);
            usuarioFecha.setTextColor(Color.DKGRAY);

            contenedor.addView(usuarioFecha);

            TextView comentario =
                    new TextView(requireContext());

            comentario.setText(
                    "Comentario: " + cambio.comentario);

            comentario.setTextSize(13);
            comentario.setPadding(0, 4, 0, 8);

            contenedor.addView(comentario);
        }
    }

    private void mostrarError(String mensaje) {
        binding.detailTitle.setText(
                "No se pudo cargar el incidente");

        Snackbar.make(binding.getRoot(),
                mensaje != null ? mensaje : "Error desconocido",
                Snackbar.LENGTH_LONG).show();
    }


    private void mostrarDialogoCambioEstado() {

        String nuevoEstado;

        if ("Abierto".equalsIgnoreCase(estadoActual)) {
            nuevoEstado = "En proceso";
        } else if ("En proceso".equalsIgnoreCase(estadoActual)) {
            nuevoEstado = "Resuelto";
        } else {
            return;
        }

        EditText inputComentario = new EditText(requireContext());
        inputComentario.setHint("Comentario obligatorio");
        inputComentario.setMinLines(3);
        inputComentario.setInputType(
                InputType.TYPE_CLASS_TEXT |
                        InputType.TYPE_TEXT_FLAG_MULTI_LINE
        );

        LinearLayout contenedor = new LinearLayout(requireContext());
        contenedor.setOrientation(LinearLayout.VERTICAL);

        int espacio = (int) (24 *
                getResources().getDisplayMetrics().density);

        contenedor.setPadding(espacio, 8, espacio, 8);
        contenedor.addView(inputComentario);

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar a " + nuevoEstado)
                .setView(contenedor)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialogo.setOnShowListener(d -> {

            dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        String comentario = inputComentario.getText()
                                .toString().trim();

                        if (comentario.isEmpty()) {
                            inputComentario.setError(
                                    "El comentario es obligatorio");
                            return;
                        }

                        enviarCambioEstado(
                                nuevoEstado, comentario, dialogo);
                    });
        });

        dialogo.show();
    }

    private void enviarCambioEstado(
            String nuevoEstado,
            String comentario,
            AlertDialog dialogo) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");

        Map<String, String> datos = new HashMap<>();
        datos.put("estado", nuevoEstado);
        datos.put("comentario", comentario);

        Call<RptaInsertarIncidente> call =
                api.cambiarEstadoIncidente(
                        "JWT " + token,
                        idIncidenteActual,
                        datos
                );

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);

        call.enqueue(new Callback<RptaInsertarIncidente>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Response<RptaInsertarIncidente> response) {

                if (!isAdded() || binding == null) {
                    dialogo.dismiss();
                    return;
                }

                RptaInsertarIncidente rpta = response.body();

                if (response.isSuccessful()
                        && rpta != null
                        && rpta.getCode() == 1) {

                    dialogo.dismiss();

                    Snackbar.make(binding.getRoot(),
                            "Estado actualizado correctamente",
                            Snackbar.LENGTH_LONG).show();

                    cargarDetalle(idIncidenteActual);

                } else {

                    dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setEnabled(true);

                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "Error HTTP: " + response.code();

                    Snackbar.make(binding.getRoot(),
                            mensaje != null ? mensaje : "Error al actualizar",
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) {
                    dialogo.dismiss();
                    return;
                }

                dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setEnabled(true);

                Snackbar.make(binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void cargarTecnicos() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");

        Call<RptaObtenerUsuarios> call =
                api.buscarUsuariosPorRol(
                        "JWT " + token,
                        "Técnico"
                );

        call.enqueue(new Callback<RptaObtenerUsuarios>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaObtenerUsuarios> call,
                    @NonNull Response<RptaObtenerUsuarios> response) {

                if (!isAdded() || binding == null) return;

                if (!response.isSuccessful() || response.body() == null) {
                    Snackbar.make(binding.getRoot(),
                            "Error HTTP: " + response.code(),
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                RptaObtenerUsuarios rpta = response.body();

                if (rpta.getCode() != 1 || rpta.getData() == null) {
                    Snackbar.make(binding.getRoot(),
                            rpta.getMsge() != null
                                    ? rpta.getMsge()
                                    : "Error al obtener técnicos",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                List<RptaObtenerUsuarios.Usuario> tecnicosActivos =
                        new ArrayList<>();

                for (RptaObtenerUsuarios.Usuario usuario
                        : rpta.getData()) {

                    if (usuario.isActivo()) {
                        tecnicosActivos.add(usuario);
                    }
                }

                if (tecnicosActivos.isEmpty()) {
                    Snackbar.make(binding.getRoot(),
                            "No hay técnicos activos registrados",
                            Snackbar.LENGTH_LONG).show();
                    return;
                }

                mostrarDialogoTecnicos(tecnicosActivos);
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaObtenerUsuarios> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                Snackbar.make(binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }


    private void mostrarDialogoTecnicos(
            List<RptaObtenerUsuarios.Usuario> tecnicos) {

        String[] nombres = new String[tecnicos.size()];

        for (int i = 0; i < tecnicos.size(); i++) {
            nombres[i] = tecnicos.get(i).getNombre();
        }

        final int[] seleccionado = {-1};

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle("Seleccionar técnico")
                .setSingleChoiceItems(nombres, -1,
                        (dialog, posicion) -> {
                            seleccionado[0] = posicion;
                        })
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Asignar", null)
                .create();

        dialogo.show();

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(v -> {

                    if (seleccionado[0] == -1) {
                        Snackbar.make(binding.getRoot(),
                                "Seleccione un técnico",
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    RptaObtenerUsuarios.Usuario tecnico =
                            tecnicos.get(seleccionado[0]);

                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirmar asignación")
                            .setMessage(
                                    "¿Asignar este incidente a "
                                            + tecnico.getNombre() + "?")
                            .setNegativeButton("Cancelar", null)
                            .setPositiveButton("Confirmar",
                                    (confirmacion, opcion) -> {
                                        dialogo.dismiss();
                                        asignarTecnico(tecnico.getId());
                                    })
                            .show();
                });
    }


    private void asignarTecnico(int idTecnico) {

        if (idIncidenteActual <= 0) {
            Snackbar.make(binding.getRoot(),
                    "No se ha seleccionado un incidente",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");

        Map<String, Integer> datos = new HashMap<>();
        datos.put("id_tecnico", idTecnico);

        Call<RptaInsertarIncidente> call =
                api.asignarTecnico(
                        "JWT " + token,
                        idIncidenteActual,
                        datos
                );

        binding.btnAsignarTecnico.setEnabled(false);

        call.enqueue(new Callback<RptaInsertarIncidente>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Response<RptaInsertarIncidente> response) {

                if (!isAdded() || binding == null) return;

                binding.btnAsignarTecnico.setEnabled(true);

                RptaInsertarIncidente rpta = response.body();

                if (response.isSuccessful()
                        && rpta != null
                        && rpta.getCode() == 1) {

                    Snackbar.make(binding.getRoot(),
                            "Técnico asignado correctamente",
                            Snackbar.LENGTH_LONG).show();

                    // Recargar el detalle desde MySQL
                    cargarDetalle(idIncidenteActual);

                } else {

                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "Error HTTP: " + response.code();

                    Snackbar.make(binding.getRoot(),
                            mensaje != null
                                    ? mensaje
                                    : "No se pudo asignar el técnico",
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                binding.btnAsignarTecnico.setEnabled(true);

                Snackbar.make(binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }




    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
