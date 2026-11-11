package com.example.py_damb2026ii;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.py_damb2026ii.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SharedPreferences prefs;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        getWindow().setStatusBarColor(
                getColor(R.color.productGridBackgroundColor));

        getWindow().setNavigationBarColor(
                getColor(R.color.softBlue));

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Ajustar el contenido para respetar las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(
                binding.getRoot(),
                (view, windowInsets) -> {

                    Insets systemBars = windowInsets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                                    | WindowInsetsCompat.Type.displayCutout()
                    );

                    view.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return windowInsets;
                }
        );

        ViewCompat.requestApplyInsets(binding.getRoot());

        prefs = getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);

        if (prefs.getString("TOKEN_JWT", "").isEmpty()) {
            salir();
            return;
        }

        setSupportActionBar(binding.toolbar);

        NavHostFragment host = (NavHostFragment)
                getSupportFragmentManager().findFragmentById(
                        R.id.nav_host_fragment_content_main);

        if (host == null) return;

        navController = host.getNavController();

        String rol = prefs.getString("USER_ROLE", "").trim();

        boolean esAdministradorEmpresa =
                "Administrador".equalsIgnoreCase(rol);

// COMPROBACIÓN TEMPORAL
        Snackbar.make(
                binding.getRoot(),
                "Rol detectado: [" + rol + "]",
                Snackbar.LENGTH_LONG
        ).show();

// Gestión: exclusiva del administrador.
        binding.navBranches.setVisibility(
                esAdministradorEmpresa ? View.VISIBLE : View.GONE
        );

// Mapa: exclusivo del administrador.
        binding.navMap.setVisibility(
                esAdministradorEmpresa ? View.VISIBLE : View.GONE
        );

        binding.navMap.setEnabled(esAdministradorEmpresa);
        binding.navMap.setClickable(esAdministradorEmpresa);

// Gestión: exclusiva del administrador.
        binding.navBranches.setVisibility(
                esAdministradorEmpresa ? View.VISIBLE : View.GONE
        );

// Mapa: exclusivo del administrador.
        binding.navMap.setVisibility(
                esAdministradorEmpresa ? View.VISIBLE : View.GONE
        );

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.IncidentListFragment,
                R.id.MapFragment,
                R.id.ProfileFragment,
                R.id.BranchListFragment
        ).build();

        NavigationUI.setupActionBarWithNavController(
                this,
                navController,
                appBarConfiguration
        );

// INCIDENTES
        binding.navIncidents.setOnClickListener(v ->
                volverAIncidentes()
        );

// MAPA
        binding.navMap.setOnClickListener(v -> {

            if (!esAdministradorEmpresa) return;

            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId()
                    != R.id.MapFragment) {

                navController.navigate(R.id.MapFragment);
            }
        });

// ALERTAS
        binding.navAlerts.setOnClickListener(v ->
                aviso("Alertas",
                        "Las notificaciones todavía no están habilitadas.")
        );

// GESTIÓN
        binding.navBranches.setOnClickListener(v -> {

            if (!esAdministradorEmpresa) return;

            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId()
                    != R.id.BranchListFragment) {

                navController.navigate(R.id.BranchListFragment);
            }
        });

// PERFIL
        binding.navProfile.setOnClickListener(v -> abrirPerfil());

// FAB
        binding.fab.setOnClickListener(v ->
                navController.navigate(R.id.AddIncidentFragment)
        );

        // CONTROL DE PANTALLAS
        navController.addOnDestinationChangedListener(
                (controller, destination, arguments) -> {

                    int id = destination.getId();

                    boolean enListado =
                            id == R.id.IncidentListFragment;

                    boolean enPerfil =
                            id == R.id.ProfileFragment;

                    boolean enMapa =
                            id == R.id.MapFragment;

                    boolean enGestion =
                            id == R.id.BranchListFragment;

// Toolbar
                    binding.toolbar.setVisibility(
                            (enListado || enMapa || enGestion)
                                    ? View.GONE
                                    : View.VISIBLE
                    );

// Barra inferior
                    binding.bottomBar.setVisibility(
                            (enListado || enPerfil || enMapa || enGestion)
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    // Botón para registrar incidentes:
                    // exclusivo del solicitante.
                    boolean solicitante =
                            "Solicitante".equalsIgnoreCase(
                                    prefs.getString("USER_ROLE", "")
                            );

                    binding.fab.setVisibility(
                            enListado && solicitante
                                    ? View.VISIBLE
                                    : View.GONE
                    );

                    // Título
                    if (!enListado) {

                        String titulo;

                        if (id == R.id.MapFragment) {
                            titulo = "Mapa de empresas";

                        } else if (id == R.id.ProfileFragment) {
                            titulo = "Mi perfil";

                        } else if (id == R.id.BranchListFragment) {
                            titulo = "Gestión";

                        } else if (id == R.id.AddCollaboratorFragment) {
                            titulo = "Nuevo colaborador";

                        } else if (id == R.id.AddBranchFragment) {
                            titulo = "Nueva sucursal";

                        } else if (id == R.id.AddIncidentFragment) {
                            titulo = "Nuevo incidente";

                        } else if (id == R.id.IncidentDetailFragment) {
                            titulo = "Detalle del incidente";

                        } else {
                            titulo = destination.getLabel() == null
                                    ? "IncidentApp"
                                    : destination.getLabel().toString();
                        }

                        binding.toolbar.setTitle(titulo);
                    }

                    invalidateOptionsMenu();
                });
    }

    // Comprobar el rol del administrador de empresa.
    private boolean esAdministradorEmpresa() {

        String rol = prefs.getString("USER_ROLE", "");

        return "Administrador".equalsIgnoreCase(rol);
    }

    public void solicitarCerrarSesion() {
        confirmarSalir();
    }
    private void volverAIncidentes() {
        if (navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() != R.id.IncidentListFragment) {
            navController.popBackStack(R.id.IncidentListFragment, false);
        }
    }
    private void aviso(String titulo, String texto) {
        new AlertDialog.Builder(this).setTitle(titulo).setMessage(texto).setPositiveButton("Entendido", null).show();
    }
    private void mostrarPerfil() {
        String rol = prefs.getString("USER_ROLE", "Colaborador");
        String usuario = prefs.getString("USER_NAME", "");
        new AlertDialog.Builder(this).setTitle("Mi cuenta")
                .setMessage((usuario.isEmpty() ? "IncidentApp" : usuario) + "\nRol: " + rol)
                .setNeutralButton("Cancelar", null)
                .setNegativeButton("Cerrar sesión", (d, w) -> confirmarSalir())
                .show();
    }
    private void confirmarSalir() {
        new AlertDialog.Builder(this).setTitle("Cerrar sesión")
                .setMessage("¿Deseas salir de IncidentApp?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Salir", (d,w) -> { salir(); }).show();
    }
    private void salir() {
        if (prefs != null) {
            prefs.edit().clear().apply();
        } else {
            getSharedPreferences("USAT_SP", Context.MODE_PRIVATE).edit().clear().apply();
        }

        try {
            Intent serviceIntent = new Intent(this, UbicacionTecnicoService.class);
            stopService(serviceIntent);
        } catch (Exception ignored) {}

        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); 
        finish();
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); return true;
    }
    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) { confirmarSalir(); return true; }
        return super.onOptionsItemSelected(item);
    }
    @Override public boolean onSupportNavigateUp() {
        return navController != null && NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void abrirPerfil() {

        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId()
                != R.id.ProfileFragment) {

            navController.navigate(R.id.ProfileFragment);
        }
    }
}
