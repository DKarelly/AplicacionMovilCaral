
package com.example.py_damb2026ii;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.FragmentMapBinding;
import com.example.py_damb2026ii.model.RptaObtenerSucursales;
import com.example.py_damb2026ii.network.SucursalEntry;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.example.py_damb2026ii.model.RptaObtenerIncidentes;
import com.example.py_damb2026ii.network.IncidentEntry;
import android.widget.FrameLayout;
import androidx.navigation.fragment.NavHostFragment;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;
    private SharedPreferences sharedPref;
    private GoogleMap mMap;

    private final List<SucursalEntry> listaSucursales = new ArrayList<>();
    private final List<IncidentEntry> listaIncidentes =
            new ArrayList<>();

    private boolean incidentesCargados = false;

    private final Map<Marker, SucursalEntry> sucursalPorMarcador =
            new HashMap<>();

    private Marker marcadorSeleccionado;
    private SucursalEntry sucursalSeleccionada;

    public MapFragment() {
        // Constructor vacío
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        sharedPref = requireActivity().getSharedPreferences(
                "USAT_SP", Context.MODE_PRIVATE
        );

        // SEGURIDAD: el mapa es exclusivo del administrador.
        String rolActual = sharedPref.getString(
                "USER_ROLE", ""
        ).trim();

        if (!"Administrador".equalsIgnoreCase(rolActual)) {

            FrameLayout pantallaBloqueada =
                    new FrameLayout(requireContext());

            pantallaBloqueada.post(() -> {

                if (!isAdded()) return;

                Snackbar.make(
                        pantallaBloqueada,
                        "No tienes permiso para acceder al mapa.",
                        Snackbar.LENGTH_SHORT
                ).show();

                NavHostFragment.findNavController(this)
                        .navigate(R.id.IncidentListFragment);
            });

            return pantallaBloqueada;
        }

        binding = FragmentMapBinding.inflate(inflater, container, false);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Buscador
        binding.etBuscarSucursal.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                filtrarSucursales(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Botón para abrir la ubicación
        binding.btnComoLlegar.setOnClickListener(v -> abrirUbicacion());

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        // Controles del mapa
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        // Mostrar el mapa normal con calles y nombres
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Reservar espacio para que el zoom no quede
        // detrás de la tarjeta inferior
        mMap.setPadding(0, 0, dp(8), dp(220));

        // Estilo visual
        try {
            mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            requireContext(), R.raw.map_style
                    )
            );
        } catch (Exception e) {
            mostrarMensaje("No se pudo aplicar el estilo del mapa.");
        }

        // Seleccionar sucursal al tocar un marcador
        mMap.setOnMarkerClickListener(marker -> {

            SucursalEntry sucursal = sucursalPorMarcador.get(marker);

            if (sucursal != null) {
                seleccionarSucursal(sucursal, marker);
                return true;
            }

            return false;
        });

        cargarSucursales();
        cargarIncidentes();
    }

    private void cargarSucursales() {

        String token = sharedPref.getString("TOKEN_JWT", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        Call<RptaObtenerSucursales> call =
                api.obtenerSucursales("JWT " + token);

        call.enqueue(new Callback<RptaObtenerSucursales>() {

            @Override
            public void onResponse(
                    @NonNull Call<RptaObtenerSucursales> call,
                    @NonNull Response<RptaObtenerSucursales> response) {

                if (binding == null || mMap == null) return;

                if (!response.isSuccessful()) {
                    mostrarMensaje(
                            "Error al cargar sucursales: " + response.code()
                    );
                    return;
                }

                RptaObtenerSucursales rpta = response.body();

                if (rpta != null && rpta.getCode() == 1
                        && rpta.getData() != null) {

                    listaSucursales.clear();
                    listaSucursales.addAll(rpta.getData());

                    filtrarSucursales("");

                } else {
                    mostrarMensaje("No se encontraron sucursales.");
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<RptaObtenerSucursales> call,
                    @NonNull Throwable t) {

                if (binding != null) {
                    mostrarMensaje("Error de conexión: " + t.getMessage());
                }
            }
        });
    }

    // Filtrar sucursales por nombre o dirección
    private void filtrarSucursales(String texto) {

        if (mMap == null || binding == null) return;

        mMap.clear();
        sucursalPorMarcador.clear();
        marcadorSeleccionado = null;
        sucursalSeleccionada = null;

        String busqueda = texto.trim().toLowerCase(Locale.ROOT);

        LatLngBounds.Builder limites = new LatLngBounds.Builder();

        Marker primerMarcador = null;
        SucursalEntry primeraSucursal = null;
        LatLng primeraUbicacion = null;

        int contador = 0;

        for (SucursalEntry sucursal : listaSucursales) {

            if (sucursal == null) continue;

            double lat = sucursal.latitud;
            double lon = sucursal.longitud;

            // Validar coordenadas
            if (lat == 0.0 || lon == 0.0) continue;

            if (lat < -90 || lat > 90 ||
                    lon < -180 || lon > 180) continue;

            String nombre = sucursal.nombre != null
                    ? sucursal.nombre : "Sucursal";

            String direccion = sucursal.direccion != null
                    ? sucursal.direccion : "Sin dirección";

            String contenido = (nombre + " " + direccion)
                    .toLowerCase(Locale.ROOT);

            // Aplicar búsqueda
            if (!busqueda.isEmpty() &&
                    !contenido.contains(busqueda)) {
                continue;
            }

            LatLng ubicacion = new LatLng(lat, lon);

            // Crear marcador azul
            Marker marcador = mMap.addMarker(
                    new com.google.android.gms.maps.model.MarkerOptions()
                            .position(ubicacion)
                            .title(nombre)
                            .snippet(direccion)
                            .icon(BitmapDescriptorFactory.defaultMarker(
                                    BitmapDescriptorFactory.HUE_AZURE
                            ))
            );

            if (marcador == null) continue;

            sucursalPorMarcador.put(marcador, sucursal);

            limites.include(ubicacion);

            if (primerMarcador == null) {
                primerMarcador = marcador;
                primeraSucursal = sucursal;
                primeraUbicacion = ubicacion;
            }

            contador++;
        }

        binding.txtCantidadSucursales.setText(
                contador + (contador == 1
                        ? " ubicación"
                        : " ubicaciones")
        );

        // Sin resultados
        if (contador == 0) {

            binding.txtNombreSeleccionado.setText(
                    "No encontramos sucursales"
            );

            binding.txtDireccionSeleccionada.setText(
                    "Prueba con otro nombre o dirección."
            );

            binding.txtEstadoUbicacion.setText(
                    "Sin resultados"
            );

            binding.btnComoLlegar.setEnabled(false);
            binding.btnComoLlegar.setAlpha(0.5f);
            binding.txtIncidentesSucursal.setText(
                    "Sin sucursal seleccionada");

            binding.txtDetalleIncidentesSucursal.setText(
                    "No hay incidentes para consultar");

            return;
        }

        binding.btnComoLlegar.setEnabled(true);
        binding.btnComoLlegar.setAlpha(1f);

        // Ajustar cámara
        if (contador == 1) {

            mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                            primeraUbicacion, 15f
                    )
            );

        } else {

            try {
                mMap.animateCamera(
                        CameraUpdateFactory.newLatLngBounds(
                                limites.build(), 150
                        )
                );
            } catch (Exception e) {
                mMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                                primeraUbicacion, 12f
                        )
                );
            }
        }

        // Mostrar información de la primera sucursal
        seleccionarSucursal(primeraSucursal, primerMarcador);
    }

    // Actualizar tarjeta inferior
    private void seleccionarSucursal(
            SucursalEntry sucursal, Marker marcador) {

        if (binding == null || sucursal == null) return;

        // Restaurar color del marcador anterior
        if (marcadorSeleccionado != null) {

            marcadorSeleccionado.setIcon(
                    BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_AZURE
                    )
            );
        }

        marcadorSeleccionado = marcador;
        sucursalSeleccionada = sucursal;

        // Destacar marcador seleccionado
        if (marcadorSeleccionado != null) {

            marcadorSeleccionado.setIcon(
                    BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                    )
            );
        }

        binding.txtNombreSeleccionado.setText(
                sucursal.nombre != null
                        ? sucursal.nombre : "Sucursal"
        );

        binding.txtDireccionSeleccionada.setText(
                sucursal.direccion != null
                        ? sucursal.direccion : "Sin dirección"
        );

        binding.txtEstadoUbicacion.setText(
                "Ubicación registrada"
        );

        binding.btnComoLlegar.setText("Abrir ubicación");
        binding.btnComoLlegar.setEnabled(true);
        binding.btnComoLlegar.setAlpha(1f);
        // Actualizar los incidentes de la sucursal elegida
        actualizarContadorSucursal();
    }

    // Abrir la ubicación seleccionada en una aplicación de mapas
    private void abrirUbicacion() {

        if (sucursalSeleccionada == null) return;

        double lat = sucursalSeleccionada.latitud;
        double lon = sucursalSeleccionada.longitud;

        String nombre = sucursalSeleccionada.nombre != null
                ? sucursalSeleccionada.nombre : "Sucursal";

        Uri uri = Uri.parse(
                "geo:" + lat + "," + lon +
                        "?q=" + lat + "," + lon +
                        "(" + Uri.encode(nombre) + ")"
        );

        Intent intent = new Intent(Intent.ACTION_VIEW, uri);

        if (intent.resolveActivity(
                requireActivity().getPackageManager()) != null) {

            startActivity(intent);

        } else {
            mostrarMensaje("No hay una aplicación de mapas disponible.");
        }
    }


    private void cargarIncidentes() {

        incidentesCargados = false;

        String token = sharedPref.getString("TOKEN_JWT", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        JCPyAnyApi api = retrofit.create(JCPyAnyApi.class);

        api.obtenerIncidentes("JWT " + token)
                .enqueue(new Callback<RptaObtenerIncidentes>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<RptaObtenerIncidentes> call,
                            @NonNull Response<RptaObtenerIncidentes> response) {

                        if (!isAdded() || binding == null) return;

                        RptaObtenerIncidentes rpta = response.body();

                        if (!response.isSuccessful()
                                || rpta == null
                                || rpta.getCode() != 1
                                || rpta.getData() == null) {

                            incidentesCargados = false;
                            actualizarContadorSucursal();
                            mostrarMensaje(
                                    "No se pudieron consultar los incidentes");
                            return;
                        }

                        listaIncidentes.clear();
                        listaIncidentes.addAll(rpta.getData());

                        incidentesCargados = true;

                        actualizarContadorSucursal();
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<RptaObtenerIncidentes> call,
                            @NonNull Throwable throwable) {

                        if (!isAdded() || binding == null) return;

                        incidentesCargados = false;
                        actualizarContadorSucursal();

                        mostrarMensaje(
                                "No se pudo conectar para consultar incidentes");
                    }
                });
    }


    private void actualizarContadorSucursal() {

        if (binding == null) return;

        if (sucursalSeleccionada == null) {
            binding.txtIncidentesSucursal.setText(
                    "Selecciona una sucursal");

            binding.txtDetalleIncidentesSucursal.setText(
                    "Toca un marcador del mapa");
            return;
        }

        if (!incidentesCargados) {
            binding.txtIncidentesSucursal.setText(
                    "Incidentes no disponibles");

            binding.txtDetalleIncidentesSucursal.setText(
                    "No se pudo obtener la información del servidor");
            return;
        }

        int abiertos = 0;
        int enProceso = 0;

        for (IncidentEntry incidente : listaIncidentes) {

            if (incidente == null) continue;

            if (incidente.id_sucursal != sucursalSeleccionada.id) {
                continue;
            }

            String estado = incidente.estado == null
                    ? ""
                    : incidente.estado.trim();

            if ("Abierto".equalsIgnoreCase(estado)) {
                abiertos++;

            } else if ("En proceso".equalsIgnoreCase(estado)) {
                enProceso++;
            }
        }

        int pendientes = abiertos + enProceso;

        binding.txtIncidentesSucursal.setText(
                pendientes + (pendientes == 1
                        ? " incidente pendiente"
                        : " incidentes pendientes")
        );

        binding.txtDetalleIncidentesSucursal.setText(
                "Abiertos: " + abiertos
                        + "   |   En proceso: " + enProceso
        );
    }


    private void mostrarMensaje(String mensaje) {

        if (binding != null) {

            Snackbar.make(
                    binding.getRoot(),
                    mensaje,
                    Snackbar.LENGTH_LONG
            ).show();
        }
    }
    private int dp(int valor) {
        return (int) (valor *
                getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        binding = null;
        mMap = null;

        sucursalPorMarcador.clear();
        marcadorSeleccionado = null;
        sucursalSeleccionada = null;
    }
}
