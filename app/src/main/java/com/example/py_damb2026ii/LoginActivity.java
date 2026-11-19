package com.example.py_damb2026ii;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.py_damb2026ii.databinding.ActivityLoginBinding;
import com.example.py_damb2026ii.repository.SupabaseAuthRepository;
import com.example.py_damb2026ii.repository.AuthCallback;
import com.example.py_damb2026ii.repository.MobileUser;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import android.graphics.Color;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences checkPrefs = getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        String tokenPersistente = checkPrefs.getString("TOKEN_JWT", "");
        if (!tokenPersistente.isEmpty()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        getWindow().setStatusBarColor(
                getColor(R.color.productGridBackgroundColor));

        getWindow().setNavigationBarColor(
                getColor(R.color.productGridBackgroundColor));

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        prefs = getSharedPreferences(
                "USAT_SP", Context.MODE_PRIVATE);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Título IncidentApp en dos colores
        configurarTitulo();

        // Recuperación de contraseña: función pendiente
        binding.txtForgot.setOnClickListener(v -> {
            Snackbar.make(
                    binding.getRoot(),
                    "La recuperación de contraseña estará disponible próximamente.",
                    Snackbar.LENGTH_LONG
            ).show();
        });

        // Inicio de sesión
        binding.nextButton.setOnClickListener(v -> {

            String usuario = binding.usernameEditText.getText() == null
                    ? ""
                    : binding.usernameEditText.getText()
                    .toString().trim();

            String clave = binding.passwordEditText.getText() == null
                    ? ""
                    : binding.passwordEditText.getText()
                    .toString();

            if (usuario.isEmpty() || clave.isEmpty()) {
                binding.txtRptaJson.setText(
                        "Ingresa usuario y contraseña");
                return;
            }

            binding.nextButton.setEnabled(false);
            binding.nextButton.setText("Ingresando...");
            binding.txtRptaJson.setText("");

            SupabaseAuthRepository.INSTANCE.login(usuario, clave, new AuthCallback() {
                @Override
                public void onSuccess(
                        @NonNull MobileUser user,
                        @NonNull java.util.List<com.example.py_damb2026ii.repository.MobileMembership> memberships,
                        @NonNull String accessToken,
                        @NonNull String refreshToken) {

                    String areasStr = "";
                    if (user.getAreasAutorizadasList() != null && !user.getAreasAutorizadasList().isEmpty()) {
                        areasStr = android.text.TextUtils.join(",", user.getAreasAutorizadasList());
                    }

                    // Guardar sesión
                    SharedPreferences.Editor editor = prefs.edit()
                            .putString("TOKEN_JWT", accessToken)
                            .putString("REFRESH_TOKEN", refreshToken)
                            .putString("USER_ROLE", user.getRol())
                            .putString("USER_NAME", user.getNombre())
                            .putString("USER_ID", user.getId())
                            .putString("USER_EMPRESA_ID",
                                    user.getEmpresa_id() != null ? user.getEmpresa_id() : "")
                            .putBoolean("PUEDE_GESTIONAR_INCIDENTES", user.isPuedeGestionarIncidentes())
                            .putString("AREAS_AUTORIZADAS", areasStr);

                    if (user.getSucursal_id() != null) {
                        editor.putString("USER_SUCURSAL_ID", user.getSucursal_id());
                    }
                    editor.apply();

                    FirebaseMessaging.getInstance().getToken()
                            .addOnSuccessListener(token -> {
                                if (token != null && !token.isEmpty()) {
                                    prefs.edit().putString("FCM_TOKEN", token).apply();
                                }
                            });

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(@NonNull String message) {
                    binding.nextButton.setEnabled(true);
                    binding.nextButton.setText("Ingresar");
                    mostrarError(message);
                }
            }, androidx.lifecycle.LifecycleOwnerKt.getLifecycleScope(LoginActivity.this));
        });
    }

    private void configurarTitulo() {

        SpannableString titulo =
                new SpannableString("IncidentApp");

        int colorIncident = ContextCompat.getColor(
                this, R.color.textPrimary);

        int colorApp = ContextCompat.getColor(
                this, R.color.colorAccent);

        // Incident: azul oscuro
        titulo.setSpan(
                new ForegroundColorSpan(colorIncident),
                0, 8,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // App: azul claro
        titulo.setSpan(
                new ForegroundColorSpan(colorApp),
                8, 11,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        binding.loginTitle.setText(titulo);
    }




    private void mostrarError(String mensaje) {

        binding.txtRptaJson.setText(mensaje);

        Snackbar snackbar = Snackbar.make(
                binding.getRoot(),
                mensaje,
                Snackbar.LENGTH_LONG
        );

        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#243743"));

        android.widget.TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        
        snackbar.show();
    }

}
