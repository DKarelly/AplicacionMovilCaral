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

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private SharedPreferences prefs;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.productGridBackgroundColor));
        getWindow().setNavigationBarColor(getColor(R.color.softBlue));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        prefs = getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        if (prefs.getString("TOKEN_JWT", "").isEmpty()) { salir(); return; }
        setSupportActionBar(binding.toolbar);
        NavHostFragment host = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        if (host == null) return;
        navController = host.getNavController();
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.IncidentListFragment,
                R.id.MapFragment,
                R.id.ProfileFragment
        ).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        binding.navIncidents.setOnClickListener(v -> volverAIncidentes());
        binding.navMap.setOnClickListener(v -> {

            if (navController.getCurrentDestination() != null
                    && navController.getCurrentDestination().getId()
                    != R.id.MapFragment) {

                navController.navigate(R.id.MapFragment);
            }
        });
        binding.navAlerts.setOnClickListener(v -> aviso("Alertas", "Las notificaciones todavía no están habilitadas."));
        binding.navProfile.setOnClickListener(v -> abrirPerfil());
        binding.fab.setOnClickListener(v -> navController.navigate(R.id.AddIncidentFragment));
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id = destination.getId();
            boolean enListado = id == R.id.IncidentListFragment;
            boolean enPerfil = id == R.id.ProfileFragment;
            boolean enMapa = id == R.id.MapFragment;

            binding.toolbar.setVisibility(
                    (enListado || enMapa) ? View.GONE : View.VISIBLE
            );

            binding.bottomBar.setVisibility(
                    (enListado || enPerfil || enMapa)
                            ? View.VISIBLE : View.GONE
            );
            boolean solicitante = "Solicitante".equalsIgnoreCase(prefs.getString("USER_ROLE", ""));
            binding.fab.setVisibility(enListado && solicitante ? View.VISIBLE : View.GONE);
            if (!enListado) {

                String titulo = id == R.id.MapFragment ? "Mapa de empresas"
                        : id == R.id.ProfileFragment ? "Mi perfil"
                          : id == R.id.AddIncidentFragment ? "Nuevo incidente"
                            : id == R.id.IncidentDetailFragment ? "Detalle del incidente"
                              : id == R.id.AddBranchFragment ? "Nueva sucursal"
                                : destination.getLabel() == null
                                  ? "IncidentApp"
                                  : destination.getLabel().toString();

                binding.toolbar.setTitle(titulo);
            }
            invalidateOptionsMenu();
        });
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
                .setPositiveButton("Salir", (d,w) -> { prefs.edit().clear().apply(); salir(); }).show();
    }
    private void salir() {
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i); finish();
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
