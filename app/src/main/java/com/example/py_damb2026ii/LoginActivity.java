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

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.ActivityLoginBinding;
import com.example.py_damb2026ii.model.RequAuth;
import com.example.py_damb2026ii.model.RptaAuth;
import com.google.android.material.snackbar.Snackbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
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

            RequAuth req = new RequAuth();
            req.setUsername(usuario);
            req.setPassword(clave);

            JCPyAnyApi api = new Retrofit.Builder()
                    .baseUrl(getString(R.string.dominioWS))
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .build()
                    .create(JCPyAnyApi.class);

            api.auth(req).enqueue(new Callback<RptaAuth>() {


                @Override
                public void onResponse(
                        @NonNull Call<RptaAuth> call,
                        @NonNull Response<RptaAuth> response) {

                    binding.nextButton.setEnabled(true);
                    binding.nextButton.setText("Ingresar");

                    // Credenciales incorrectas
                    if (response.code() == 401) {
                        mostrarError(
                                "Correo o contraseña incorrectos. Inténtalo nuevamente."
                        );
                        return;
                    }

                    // Otros errores del servidor
                    if (!response.isSuccessful()) {
                        if (response.code() == 403) {
                            mostrarError(
                                    "Tu cuenta no tiene permiso para ingresar."
                            );
                        } else if (response.code() >= 500) {
                            mostrarError(
                                    "El servidor no está disponible. Inténtalo más tarde."
                            );
                        } else {
                            mostrarError(
                                    "No se pudo iniciar sesión. Verifica tus datos."
                            );
                        }
                        return;
                    }

                    RptaAuth r = response.body();

                    if (r == null
                            || r.getAccess_token() == null
                            || r.getAccess_token().isEmpty()) {

                        mostrarError(
                                "No se pudo validar tu sesión. Inténtalo nuevamente."
                        );
                        return;
                    }

                    // Guardar sesión JWT
                    prefs.edit()
                            .putString("TOKEN_JWT", r.getAccess_token())
                            .putString("USER_ROLE", r.getRol())
                            .putString("USER_NAME", r.getNombreVisible(usuario))
                            .apply();

                    Intent intent = new Intent(
                            LoginActivity.this,
                            MainActivity.class
                    );

                    startActivity(intent);
                    finish();
                }



                @Override
                public void onFailure(
                        @NonNull Call<RptaAuth> call,
                        @NonNull Throwable t) {

                    binding.nextButton.setEnabled(true);
                    binding.nextButton.setText("Ingresar");

                    mostrarError(
                            "No se pudo conectar. Comprueba tu conexión a Internet."
                    );
                }

            });
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

        snackbar.setBackgroundTint(
                Color.parseColor("#243743")
        );

        snackbar.setTextColor(Color.WHITE);
        snackbar.show();
    }

}
