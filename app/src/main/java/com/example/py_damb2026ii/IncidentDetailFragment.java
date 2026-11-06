package com.example.py_damb2026ii;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentIncidentDetailBinding;
import com.example.py_damb2026ii.model.RptaDetalleIncidente;
import com.example.py_damb2026ii.model.RptaInsertarIncidente;
import com.example.py_damb2026ii.model.RptaObtenerUsuarios;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import androidx.navigation.Navigation;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;
import android.widget.ImageView;
import android.graphics.drawable.ColorDrawable;
import com.bumptech.glide.Glide;

public class IncidentDetailFragment extends Fragment {

    private FragmentIncidentDetailBinding binding;
    private SharedPreferences sharedPref;

    private int idIncidenteActual = -1;
    private String estadoActual = "";
    private int selectedTecnicoId = -1;
    private boolean tecnicosExpandido = false;
    private String prioridadSeleccionada = "Media";
    private String prioridadOriginal = "Media";
    private static final int REQUEST_GPS_PERMISSION = 7001;

    private boolean solicitudGpsEnCurso = false;

    private final List<RptaObtenerUsuarios.Usuario> tecnicosDisponibles = new ArrayList<>();

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentIncidentDetailBinding.inflate(inflater, container, false);
        sharedPref = requireActivity().getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);

        if (getArguments() != null) {
            idIncidenteActual = getArguments().getInt("id_incidente", -1);
        }

        binding.btnCambiarEstado.setOnClickListener(v -> mostrarDialogoCambioEstado());
        binding.btnGuardarCambios.setOnClickListener(v -> guardarCambiosAdmin());
        binding.btnCerrarIncidente.setOnClickListener(v -> cerrarIncidenteAdmin());

        binding.btnPriorityAlta.setOnClickListener(v -> seleccionarPrioridad("Alta"));
        binding.btnPriorityMedia.setOnClickListener(v -> seleccionarPrioridad("Media"));
        binding.btnPriorityBaja.setOnClickListener(v -> seleccionarPrioridad("Baja"));

        if (idIncidenteActual > 0) {
            cargarDetalle(idIncidenteActual);
        } else {
            binding.detailTitle.setText("No se seleccionó un incidente");
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
                api.obtenerDetalleIncidente("JWT " + token, idIncidente);

        call.enqueue(new Callback<RptaDetalleIncidente>() {
            @Override
            public void onResponse(
                    @NonNull Call<RptaDetalleIncidente> call,
                    @NonNull Response<RptaDetalleIncidente> response) {

                if (!isAdded() || binding == null) return;

                if (!response.isSuccessful() || response.body() == null) {
                    mostrarError("Error HTTP: " + response.code());
                    return;
                }

                RptaDetalleIncidente rpta = response.body();

                if (rpta.getCode() != 1 || rpta.getData() == null || rpta.getData().incidente == null) {
                    mostrarError(rpta.getMsge());
                    return;
                }

                mostrarIncidente(rpta.getData().incidente);
                mostrarHistorial(rpta.getData().historial);
                mostrarFotos(rpta.getData().fotos);

                if (esAdministrador() && !incidenteFinalizado()) {
                    cargarTecnicosInline();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaDetalleIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                mostrarError("Error de conexión: " + throwable.getMessage());
            }
        });
    }


    private boolean incidenteFinalizado() {
        return "Resuelto".equalsIgnoreCase(estadoActual)
                || "Cerrado".equalsIgnoreCase(estadoActual);
    }

    private void mostrarIncidente(RptaDetalleIncidente.Incidente incidente) {
        binding.detailCode.setText(String.format(Locale.getDefault(), "INC-%03d", incidente.id));
        binding.detailTitle.setText(valorSeguro(incidente.titulo, "Sin título"));
        binding.detailDescription.setText(valorSeguro(incidente.descripcion, "Sin descripción"));
        binding.detailSolicitante.setText(valorSeguro(incidente.solicitante, "Sin solicitante"));
        binding.detailTecnico.setText(valorSeguro(incidente.tecnico, "Sin asignar"));
        binding.detailCategoria.setText(valorSeguro(incidente.categoria, "Sin categoría"));
        binding.detailSucursal.setText(valorSeguro(incidente.sucursal, "Sin sucursal"));
        binding.detailFecha.setText(formatearFechaCliente(incidente.fecha_registro));

        binding.detailPriority.setText(valorSeguro(incidente.prioridad, "Media"));
        binding.detailStatus.setText(valorSeguro(incidente.estado, "Abierto"));

        aplicarChipPrioridad(binding.detailPriority, incidente.prioridad);
        aplicarChipEstado(binding.detailStatus, incidente.estado);

        estadoActual = valorSeguro(incidente.estado, "Abierto");
        prioridadSeleccionada = valorSeguro(incidente.prioridad, "Media");
        prioridadOriginal = prioridadSeleccionada;
        actualizarBotonesPrioridad();

        selectedTecnicoId = incidente.id_tecnico != null ? incidente.id_tecnico : -1;

        String rol = sharedPref.getString("USER_ROLE", "");


        if ("Administrador".equalsIgnoreCase(rol)) {

            binding.detailSubtitle.setVisibility(View.GONE);
            binding.btnCambiarEstado.setVisibility(View.GONE);

            if (incidenteFinalizado()) {
                binding.layoutAdminSection.setVisibility(View.GONE);
            } else {
                binding.layoutAdminSection.setVisibility(View.VISIBLE);
            }

        } else if ("Técnico".equalsIgnoreCase(rol)) {
            binding.detailSubtitle.setText("Técnico · gestión en campo");
            binding.layoutAdminSection.setVisibility(View.GONE);

            boolean puedeCambiar =
                    "Abierto".equalsIgnoreCase(estadoActual)
                            || "En proceso".equalsIgnoreCase(estadoActual);

            binding.btnCambiarEstado.setVisibility(
                    puedeCambiar ? View.VISIBLE : View.GONE
            );
        } else {
            binding.detailSubtitle.setText("Solicitante · solo lectura");
            binding.layoutAdminSection.setVisibility(View.GONE);
            binding.btnCambiarEstado.setVisibility(View.GONE);
        }
        // SEGUIMIENTO GPS DEL TÉCNICO

        if ("Técnico".equalsIgnoreCase(rol)) {

            if ("En proceso".equalsIgnoreCase(estadoActual)) {

                solicitarPermisoYIniciarSeguimiento();

            } else if (incidenteFinalizado()) {

                detenerServicioGPS();
            }
        }
    }

    private void mostrarHistorial(List<RptaDetalleIncidente.CambioEstado> historial) {
        LinearLayout contenedor = binding.detailHistorialContainer;
        contenedor.removeAllViews();

        if (historial == null || historial.isEmpty()) {
            TextView vacio = new TextView(requireContext());
            vacio.setText("No hay cambios registrados");
            vacio.setTextColor(Color.parseColor("#6F8799"));
            vacio.setTextSize(14);
            contenedor.addView(vacio);
            return;
        }

        List<RptaDetalleIncidente.CambioEstado> lista = new ArrayList<>(historial);
        Collections.reverse(lista);

        for (int i = 0; i < lista.size(); i++) {
            RptaDetalleIncidente.CambioEstado cambio = lista.get(i);
            contenedor.addView(crearItemHistorial(cambio, i < lista.size() - 1));
        }
    }

    private View crearItemHistorial(RptaDetalleIncidente.CambioEstado cambio, boolean mostrarLinea) {
        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams filaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        filaParams.bottomMargin = dp(14);
        fila.setLayoutParams(filaParams);

        LinearLayout timeline = new LinearLayout(requireContext());
        timeline.setOrientation(LinearLayout.VERTICAL);
        timeline.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        LinearLayout.LayoutParams timelineParams = new LinearLayout.LayoutParams(dp(24), LinearLayout.LayoutParams.WRAP_CONTENT);
        timeline.setLayoutParams(timelineParams);

        View punto = new View(requireContext());
        LinearLayout.LayoutParams puntoParams = new LinearLayout.LayoutParams(dp(12), dp(12));
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(Color.WHITE);
        dot.setStroke(dp(2), Color.parseColor("#6AA5CE"));
        punto.setBackground(dot);
        timeline.addView(punto, puntoParams);

        if (mostrarLinea) {
            View linea = new View(requireContext());
            LinearLayout.LayoutParams lineaParams = new LinearLayout.LayoutParams(dp(2), dp(52));
            lineaParams.topMargin = dp(4);
            linea.setBackgroundColor(Color.parseColor("#DCE7EF"));
            timeline.addView(linea, lineaParams);
        }

        LinearLayout contenido = new LinearLayout(requireContext());
        contenido.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams contenidoParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        contenidoParams.leftMargin = dp(8);
        contenido.setLayoutParams(contenidoParams);

        TextView titulo = new TextView(requireContext());
        titulo.setText(obtenerTituloHistorial(cambio));
        titulo.setTextColor(Color.parseColor("#1B3145"));
        titulo.setTextSize(14);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);

        TextView comentario = new TextView(requireContext());
        comentario.setText(valorSeguro(cambio.comentario, "Sin comentario"));
        comentario.setTextColor(Color.parseColor("#5B7487"));
        comentario.setTextSize(12);
        comentario.setPadding(0, dp(4), 0, 0);

        TextView info = new TextView(requireContext());
        info.setText(valorSeguro(cambio.usuario, "Usuario") + " · " + formatearFechaCliente(cambio.fecha_cambio));
        info.setTextColor(Color.parseColor("#4D88B3"));
        info.setTextSize(11);
        info.setPadding(0, dp(4), 0, 0);

        contenido.addView(titulo);
        contenido.addView(comentario);
        contenido.addView(info);

        fila.addView(timeline);
        fila.addView(contenido);

        return fila;
    }

    private String obtenerTituloHistorial(RptaDetalleIncidente.CambioEstado cambio) {
        if (cambio.estado_anterior == null || cambio.estado_anterior.trim().isEmpty()) {
            return valorSeguro(cambio.estado_nuevo, "Abierto");
        }
        return valorSeguro(cambio.estado_nuevo, "Estado");
    }


    private void cargarTecnicosInline() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        String token = sharedPref.getString("TOKEN_JWT", "");

        // Consultar técnicos de la sucursal del incidente actual.
        Call<RptaObtenerUsuarios> call =
                api.obtenerTecnicosPorIncidente(
                        "JWT " + token,
                        idIncidenteActual
                );

        call.enqueue(new Callback<RptaObtenerUsuarios>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaObtenerUsuarios> call,
                    @NonNull Response<RptaObtenerUsuarios> response) {

                if (!isAdded() || binding == null) return;

                if (!response.isSuccessful() || response.body() == null) {

                    Snackbar.make(
                            binding.getRoot(),
                            "Error al obtener técnicos. HTTP " + response.code(),
                            Snackbar.LENGTH_LONG
                    ).show();

                    return;
                }

                RptaObtenerUsuarios rpta = response.body();

                if (rpta.getCode() != 1 || rpta.getData() == null) {

                    Snackbar.make(
                            binding.getRoot(),
                            rpta.getMsge() != null
                                    ? rpta.getMsge()
                                    : "No se pudieron obtener técnicos",
                            Snackbar.LENGTH_LONG
                    ).show();

                    return;
                }

                tecnicosDisponibles.clear();

                for (RptaObtenerUsuarios.Usuario usuario : rpta.getData()) {

                    if (usuario.isActivo()) {
                        tecnicosDisponibles.add(usuario);
                    }

                }

                if (tecnicosDisponibles.isEmpty()) {

                    selectedTecnicoId = -1;

                    Snackbar.make(
                            binding.getRoot(),
                            "No hay técnicos activos en la sucursal de este incidente",
                            Snackbar.LENGTH_LONG
                    ).show();

                }

                mostrarTecnicosInline();

            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaObtenerUsuarios> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                Snackbar.make(
                        binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG
                ).show();

            }

        });

    }


    private void mostrarTecnicosInline() {

        binding.adminTecnicosContainer.removeAllViews();

        if (tecnicosDisponibles.isEmpty()) {
            TextView vacio = new TextView(requireContext());
            vacio.setText("No hay técnicos activos");
            vacio.setTextColor(Color.parseColor("#6F8799"));
            vacio.setTextSize(14);
            binding.adminTecnicosContainer.addView(vacio);
            return;
        }

        String nombreSeleccionado = "Seleccionar técnico";
        String iniciales = "?";

        for (RptaObtenerUsuarios.Usuario tecnico : tecnicosDisponibles) {
            if (tecnico.getId() == selectedTecnicoId) {
                nombreSeleccionado = tecnico.getNombre();
                iniciales = obtenerIniciales(nombreSeleccionado);
                break;
            }
        }

        // Contenedor del selector
        MaterialCardView combo = new MaterialCardView(requireContext());

        combo.setRadius(dp(16));
        combo.setCardBackgroundColor(Color.WHITE);
        combo.setStrokeColor(Color.parseColor("#D5E5EE"));
        combo.setStrokeWidth(dp(1));
        combo.setCardElevation(dp(1));
        combo.setClickable(true);
        combo.setFocusable(true);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(dp(14), dp(12), dp(14), dp(12));

        // Avatar circular
        TextView avatar = new TextView(requireContext());
        avatar.setWidth(dp(42));
        avatar.setHeight(dp(42));
        avatar.setGravity(Gravity.CENTER);
        avatar.setText(iniciales);
        avatar.setTextSize(14);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);
        avatar.setTextColor(Color.parseColor("#4D88B3"));

        GradientDrawable fondoAvatar = new GradientDrawable();
        fondoAvatar.setShape(GradientDrawable.OVAL);
        fondoAvatar.setColor(Color.parseColor("#E4F2F8"));
        avatar.setBackground(fondoAvatar);

        // Nombre y subtítulo
        LinearLayout textos = new LinearLayout(requireContext());
        textos.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams textoParams =
                new LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                );

        textoParams.leftMargin = dp(12);
        textos.setLayoutParams(textoParams);

        TextView nombre = new TextView(requireContext());
        nombre.setText(nombreSeleccionado);
        nombre.setTextSize(15);
        nombre.setTypeface(Typeface.DEFAULT_BOLD);
        nombre.setTextColor(Color.parseColor("#1B3145"));
        nombre.setSingleLine(true);
        nombre.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TextView subtitulo = new TextView(requireContext());
        subtitulo.setText(
                selectedTecnicoId > 0
                        ? "Técnico seleccionado"
                        : "Elige un técnico"
        );
        subtitulo.setTextSize(12);
        subtitulo.setTextColor(Color.parseColor("#7A92A3"));

        textos.addView(nombre);
        textos.addView(subtitulo);

        // Flecha desplegable
        TextView flecha = new TextView(requireContext());
        flecha.setText(tecnicosExpandido ? "⌃" : "⌄");
        flecha.setTextSize(25);
        flecha.setTextColor(Color.parseColor("#6E8798"));
        flecha.setGravity(Gravity.CENTER);

        fila.addView(avatar);
        fila.addView(textos);
        fila.addView(flecha);

        combo.addView(fila);
        binding.adminTecnicosContainer.addView(combo);

        // Abrir o cerrar opciones
        combo.setOnClickListener(v -> {
            tecnicosExpandido = !tecnicosExpandido;
            mostrarTecnicosInline();
        });

        // Mostrar técnicos solo si está desplegado
        if (tecnicosExpandido) {

            LinearLayout opciones = new LinearLayout(requireContext());
            opciones.setOrientation(LinearLayout.VERTICAL);

            opciones.setPadding(
                    0, dp(8), 0, 0
            );

            for (RptaObtenerUsuarios.Usuario tecnico : tecnicosDisponibles) {

                boolean seleccionado =
                        tecnico.getId() == selectedTecnicoId;

                opciones.addView(
                        crearCardTecnico(tecnico, seleccionado)
                );
            }

            binding.adminTecnicosContainer.addView(opciones);
        }
    }


    private View crearCardTecnico(RptaObtenerUsuarios.Usuario tecnico, boolean seleccionado) {
        MaterialCardView card = new MaterialCardView(requireContext());

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = dp(10);
        card.setLayoutParams(cardParams);
        card.setRadius(dp(16));
        card.setStrokeWidth(dp(1));
        card.setStrokeColor(seleccionado
                ? Color.parseColor("#73A8D0")
                : Color.parseColor("#D9E6EE"));
        card.setCardBackgroundColor(seleccionado
                ? Color.parseColor("#EAF4FB")
                : Color.WHITE);
        card.setClickable(true);
        card.setFocusable(true);

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(Gravity.CENTER_VERTICAL);
        fila.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView avatar = new TextView(requireContext());
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        avatar.setLayoutParams(avatarParams);
        avatar.setGravity(Gravity.CENTER);
        avatar.setText(obtenerIniciales(tecnico.getNombre()));
        avatar.setTextColor(Color.WHITE);
        avatar.setTextSize(14);
        avatar.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable avatarBg = new GradientDrawable();
        avatarBg.setShape(GradientDrawable.OVAL);
        avatarBg.setColor(Color.parseColor("#5C9B78"));
        avatar.setBackground(avatarBg);

        LinearLayout textos = new LinearLayout(requireContext());
        textos.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams textosParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        textosParams.leftMargin = dp(12);
        textos.setLayoutParams(textosParams);

        TextView nombre = new TextView(requireContext());
        nombre.setText(valorSeguro(tecnico.getNombre(), "Técnico"));
        nombre.setTextSize(16);
        nombre.setTextColor(Color.parseColor("#1B3145"));
        nombre.setTypeface(Typeface.DEFAULT_BOLD);

        TextView subtitulo = new TextView(requireContext());
        subtitulo.setText("Técnico disponible");
        subtitulo.setTextSize(13);
        subtitulo.setTextColor(Color.parseColor("#6F8799"));

        TextView check = new TextView(requireContext());
        check.setText(seleccionado ? "✓" : "");
        check.setTextSize(20);
        check.setTextColor(Color.parseColor("#4D88B3"));
        check.setTypeface(Typeface.DEFAULT_BOLD);

        textos.addView(nombre);
        textos.addView(subtitulo);

        fila.addView(avatar);
        fila.addView(textos);
        fila.addView(check);

        card.addView(fila);

        card.setOnClickListener(v -> {
            selectedTecnicoId = tecnico.getId();
            tecnicosExpandido = false;
            mostrarTecnicosInline();
        });

        return card;
    }

    private void seleccionarPrioridad(String prioridad) {
        prioridadSeleccionada = prioridad;
        actualizarBotonesPrioridad();
    }

    private void actualizarBotonesPrioridad() {
        estilizarBotonPrioridad(binding.btnPriorityAlta, "Alta".equalsIgnoreCase(prioridadSeleccionada), "Alta");
        estilizarBotonPrioridad(binding.btnPriorityMedia, "Media".equalsIgnoreCase(prioridadSeleccionada), "Media");
        estilizarBotonPrioridad(binding.btnPriorityBaja, "Baja".equalsIgnoreCase(prioridadSeleccionada), "Baja");
    }

    private void estilizarBotonPrioridad(MaterialButton boton, boolean seleccionado, String prioridad) {
        int stroke = Color.parseColor("#D7E4ED");
        int bg = Color.WHITE;
        int fg = Color.parseColor("#5E7688");

        if (seleccionado) {
            if ("Alta".equalsIgnoreCase(prioridad)) {
                stroke = Color.parseColor("#D9867E");
                fg = Color.parseColor("#C86B63");
                bg = Color.parseColor("#FFF7F6");
            } else if ("Media".equalsIgnoreCase(prioridad)) {
                stroke = Color.parseColor("#D6A247");
                fg = Color.parseColor("#C38A15");
                bg = Color.parseColor("#FFF9ED");
            } else {
                stroke = Color.parseColor("#7BB08D");
                fg = Color.parseColor("#5C9B78");
                bg = Color.parseColor("#F4FBF6");
            }
        }

        boton.setStrokeColor(ColorStateList.valueOf(stroke));
        boton.setStrokeWidth(dp(1));
        boton.setBackgroundTintList(ColorStateList.valueOf(bg));
        boton.setTextColor(fg);
    }


    private void guardarCambiosAdmin() {

        if (incidenteFinalizado()) {
            Snackbar.make(
                    binding.getRoot(),
                    "Este incidente ya está finalizado y no admite modificaciones.",
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        if (selectedTecnicoId <= 0) {
            Snackbar.make(binding.getRoot(),
                    "Selecciona un técnico",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        if (!prioridadSeleccionada.equalsIgnoreCase(prioridadOriginal)) {
            Snackbar.make(binding.getRoot(),
                    "La prioridad ya quedó lista visualmente, pero para guardarla en BD falta un endpoint en Flask.",
                    Snackbar.LENGTH_LONG).show();
        }

        asignarTecnico(selectedTecnicoId);
    }

    private void cerrarIncidenteAdmin() {

        if (incidenteFinalizado()) {
            Snackbar.make(
                    binding.getRoot(),
                    "Este incidente ya está finalizado y no admite modificaciones.",
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

        if ("Resuelto".equalsIgnoreCase(estadoActual) || "Cerrado".equalsIgnoreCase(estadoActual)) {
            Snackbar.make(binding.getRoot(),
                    "El incidente ya está cerrado",
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        enviarCambioEstadoDirecto("Resuelto", "Incidente cerrado por administrador");
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
        contenedor.setPadding(dp(24), dp(8), dp(24), dp(8));
        contenedor.addView(inputComentario);

        AlertDialog dialogo = new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar a " + nuevoEstado)
                .setView(contenedor)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();

        dialogo.setOnShowListener(d ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(v -> {
                            String comentario = inputComentario.getText().toString().trim();

                            if (comentario.isEmpty()) {
                                inputComentario.setError("El comentario es obligatorio");
                                return;
                            }

                            enviarCambioEstado(nuevoEstado, comentario, dialogo);
                        })
        );

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
                api.cambiarEstadoIncidente("JWT " + token, idIncidenteActual, datos);

        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

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

                if (response.isSuccessful() && rpta != null && rpta.getCode() == 1) {
                    dialogo.dismiss();

                    Snackbar.make(binding.getRoot(),
                            "Estado actualizado correctamente",
                            Snackbar.LENGTH_LONG).show();

                    cargarDetalle(idIncidenteActual);

                } else {
                    dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "Error HTTP: " + response.code();

                    Snackbar.make(binding.getRoot(),
                            valorSeguro(mensaje, "Error al actualizar"),
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

                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);

                Snackbar.make(binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void enviarCambioEstadoDirecto(String nuevoEstado, String comentario) {
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
                api.cambiarEstadoIncidente("JWT " + token, idIncidenteActual, datos);

        call.enqueue(new Callback<RptaInsertarIncidente>() {
            @Override
            public void onResponse(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Response<RptaInsertarIncidente> response) {

                if (!isAdded() || binding == null) return;

                RptaInsertarIncidente rpta = response.body();

                if (response.isSuccessful() && rpta != null && rpta.getCode() == 1) {
                    Snackbar.make(binding.getRoot(),
                            "Incidente cerrado correctamente",
                            Snackbar.LENGTH_LONG).show();
                    cargarDetalle(idIncidenteActual);
                } else {
                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "Error HTTP: " + response.code();

                    Snackbar.make(binding.getRoot(),
                            valorSeguro(mensaje, "No se pudo cerrar el incidente"),
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                Snackbar.make(binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void asignarTecnico(int idTecnico) {

        if (incidenteFinalizado()) {
            Snackbar.make(
                    binding.getRoot(),
                    "Este incidente ya está finalizado y no admite modificaciones.",
                    Snackbar.LENGTH_LONG
            ).show();
            return;
        }

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
                api.asignarTecnico("JWT " + token, idIncidenteActual, datos);

        binding.btnGuardarCambios.setEnabled(false);

        call.enqueue(new Callback<RptaInsertarIncidente>() {
            @Override
            public void onResponse(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Response<RptaInsertarIncidente> response) {

                if (!isAdded() || binding == null) return;

                binding.btnGuardarCambios.setEnabled(true);

                RptaInsertarIncidente rpta = response.body();

                if (response.isSuccessful() && rpta != null && rpta.getCode() == 1) {

                    android.widget.Toast.makeText(
                            requireContext(),
                            "Cambios guardados correctamente",
                            android.widget.Toast.LENGTH_SHORT
                    ).show();

                    androidx.navigation.NavController navController =
                            Navigation.findNavController(binding.getRoot());

                    if (!navController.popBackStack(
                            R.id.IncidentListFragment, false)) {

                        navController.navigate(R.id.IncidentListFragment);
                    }

                } else {
                    String mensaje = rpta != null
                            ? rpta.getMsge()
                            : "Error HTTP: " + response.code();

                    Snackbar.make(binding.getRoot(),
                            valorSeguro(mensaje, "No se pudo asignar el técnico"),
                            Snackbar.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaInsertarIncidente> call,
                    @NonNull Throwable throwable) {

                if (!isAdded() || binding == null) return;

                binding.btnGuardarCambios.setEnabled(true);

                Snackbar.make(binding.getRoot(),
                        "Error de conexión: " + throwable.getMessage(),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void aplicarChipPrioridad(TextView textView, String prioridad) {
        int bg = Color.parseColor("#F6E5E1");
        int fg = Color.parseColor("#D07B6B");

        if ("Media".equalsIgnoreCase(prioridad)) {
            bg = Color.parseColor("#F7EFD9");
            fg = Color.parseColor("#C38A15");
        } else if ("Baja".equalsIgnoreCase(prioridad)) {
            bg = Color.parseColor("#E4F2E8");
            fg = Color.parseColor("#5C9B78");
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bg);
        drawable.setCornerRadius(dp(20));
        textView.setBackground(drawable);
        textView.setTextColor(fg);
    }

    private void aplicarChipEstado(TextView textView, String estado) {
        int bg = Color.parseColor("#EAF4FB");
        int fg = Color.parseColor("#4D88B3");

        if ("Resuelto".equalsIgnoreCase(estado) || "Cerrado".equalsIgnoreCase(estado)) {
            bg = Color.parseColor("#E7F3EA");
            fg = Color.parseColor("#5C9B78");
        } else if ("Abierto".equalsIgnoreCase(estado)) {
            bg = Color.parseColor("#EEF4F7");
            fg = Color.parseColor("#6C8698");
        }

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(bg);
        drawable.setCornerRadius(dp(20));
        textView.setBackground(drawable);
        textView.setTextColor(fg);
    }

    private void mostrarError(String mensaje) {
        binding.detailTitle.setText("No se pudo cargar el incidente");

        Snackbar.make(binding.getRoot(),
                valorSeguro(mensaje, "Error desconocido"),
                Snackbar.LENGTH_LONG).show();
    }

    private boolean esAdministrador() {
        String rol = sharedPref.getString("USER_ROLE", "");
        return "Administrador".equalsIgnoreCase(rol);
    }

    private String valorSeguro(String valor, String defecto) {
        return (valor != null && !valor.trim().isEmpty()) ? valor : defecto;
    }

    private String obtenerIniciales(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "TE";

        String[] partes = nombre.trim().split("\\s+");

        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }

        return ("" + partes[0].charAt(0) + partes[1].charAt(0)).toUpperCase();
    }

    private String formatearFechaCliente(String fechaApi) {
        if (fechaApi == null || fechaApi.trim().isEmpty()) return "";

        try {
            SimpleDateFormat entrada =
                    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            entrada.setTimeZone(TimeZone.getTimeZone("GMT"));

            Date fecha = entrada.parse(fechaApi);

            if (fecha == null) return fechaApi;

            SimpleDateFormat salida =
                    new SimpleDateFormat("dd MMM yyyy · h:mm a", new Locale("es", "PE"));

            String resultado = salida.format(fecha);
            resultado = resultado.replace("AM", "a. m.").replace("PM", "p. m.");

            return resultado;
        } catch (Exception e) {
            return fechaApi;
        }
    }


    private void solicitarPermisoYIniciarSeguimiento() {

        if (!isAdded() || idIncidenteActual <= 0) return;

        boolean permisoPreciso =
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean permisoAproximado =
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (permisoPreciso || permisoAproximado) {

            iniciarServicioGPS();
            return;
        }

        if (!solicitudGpsEnCurso) {

            solicitudGpsEnCurso = true;

            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_GPS_PERMISSION
            );
        }
    }


    private void iniciarServicioGPS() {

        if (!isAdded() || idIncidenteActual <= 0) return;

        Intent intent = new Intent(
                requireContext(),
                UbicacionTecnicoService.class
        );

        intent.setAction(UbicacionTecnicoService.ACTION_INICIAR);

        intent.putExtra(
                UbicacionTecnicoService.EXTRA_ID_INCIDENTE,
                idIncidenteActual
        );

        intent.putExtra(
                UbicacionTecnicoService.EXTRA_TOKEN,
                sharedPref.getString("TOKEN_JWT", "")
        );

        try {

            ContextCompat.startForegroundService(
                    requireContext(),
                    intent
            );

            Snackbar.make(
                    binding.getRoot(),
                    "Compartiendo ubicación durante la atención.",
                    Snackbar.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            Snackbar.make(
                    binding.getRoot(),
                    "No se pudo iniciar el seguimiento GPS: " + e.getMessage(),
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }


    private void detenerServicioGPS() {

        if (!isAdded()) return;

        Intent intent = new Intent(
                requireContext(),
                UbicacionTecnicoService.class
        );

        intent.setAction(UbicacionTecnicoService.ACTION_DETENER);

        intent.putExtra(
                UbicacionTecnicoService.EXTRA_ID_INCIDENTE,
                idIncidenteActual
        );

        requireContext().startService(intent);
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode != REQUEST_GPS_PERMISSION) return;

        solicitudGpsEnCurso = false;

        boolean permisoPreciso =
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        boolean permisoAproximado =
                ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED;

        if (permisoPreciso || permisoAproximado) {

            if ("Técnico".equalsIgnoreCase(
                    sharedPref.getString("USER_ROLE", ""))
                    && "En proceso".equalsIgnoreCase(estadoActual)) {

                iniciarServicioGPS();
            }

        } else {

            if (binding != null) {

                Snackbar.make(
                        binding.getRoot(),
                        "Sin permiso GPS. El incidente seguirá funcionando, pero no compartirás tu ubicación.",
                        Snackbar.LENGTH_LONG
                ).show();
            }
        }
    }

    private String construirUrlFoto(String ruta) {

        String base = getString(R.string.dominioWS);

        if (!base.endsWith("/")) {
            base += "/";
        }

        if (ruta.startsWith("/")) {
            ruta = ruta.substring(1);
        }

        return base + ruta;
    }
    private void mostrarFotos(List<RptaDetalleIncidente.Foto> fotos) {

        if (binding == null) return;

        LinearLayout contenedor = binding.detailFotosContainer;

        contenedor.removeAllViews();

        if (fotos == null || fotos.isEmpty()) {
            binding.detailFotosCard.setVisibility(View.GONE);
            return;
        }

        int contador = 1;

        for (RptaDetalleIncidente.Foto foto : fotos) {

            if (foto == null ||
                    foto.ruta_archivo == null ||
                    foto.ruta_archivo.trim().isEmpty()) {
                continue;
            }

            binding.detailFotosCard.setVisibility(View.VISIBLE);

            // Título de la fotografía
            TextView titulo = new TextView(requireContext());

            titulo.setText("Fotografía " + contador);
            titulo.setTextColor(Color.parseColor("#5B7487"));
            titulo.setTextSize(13);

            LinearLayout.LayoutParams tituloParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            tituloParams.bottomMargin = dp(8);

            contenedor.addView(titulo, tituloParams);

            // Imagen
            ImageView imagen = new ImageView(requireContext());

            LinearLayout.LayoutParams imagenParams =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(190)
                    );

            imagenParams.bottomMargin = dp(16);

            imagen.setLayoutParams(imagenParams);
            imagen.setScaleType(ImageView.ScaleType.CENTER_CROP);

            imagen.setBackground(
                    new ColorDrawable(Color.parseColor("#EAF4FB"))
            );

            contenedor.addView(imagen);

            // Dirección de la imagen en PythonAnywhere
            String url = construirUrlFoto(foto.ruta_archivo);

            Glide.with(this)
                    .load(url)
                    .placeholder(new ColorDrawable(Color.LTGRAY))
                    .error(new ColorDrawable(Color.LTGRAY))
                    .centerCrop()
                    .into(imagen);

            contador++;
        }

        if (contador == 1) {
            binding.detailFotosCard.setVisibility(View.GONE);
        }
    }


    private int dp(int valor) {
        return (int) (valor * requireContext().getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}