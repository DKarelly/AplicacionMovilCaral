package com.example.py_damb2026ii;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.ActivityLoginBinding;
import com.example.py_damb2026ii.model.RequAuth;
import com.example.py_damb2026ii.model.RptaAuth;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPref = this.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configurar Spinner de Roles
        String[] roles = {"Administrador", "Técnico", "Solicitante"};
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRolLogin.setAdapter(roleAdapter);

        binding.nextButton.setOnClickListener(v -> {
            binding.nextButton.setEnabled(false);

            String username = binding.usernameEditText.getText() != null ? binding.usernameEditText.getText().toString().trim() : "";
            String password = binding.passwordEditText.getText() != null ? binding.passwordEditText.getText().toString().trim() : "";
            String selectedRole = binding.spinnerRolLogin.getSelectedItem() != null ? binding.spinnerRolLogin.getSelectedItem().toString() : "Administrador";

            obtenerTokenApi(username, password, selectedRole, () -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }

    private void obtenerTokenApi(String username, String password, String selectedRole, Runnable onTokenSaved) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.dominioWS))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        RequAuth requAuth = new RequAuth();
        requAuth.setUsername(username);
        requAuth.setPassword(password);
        Call<RptaAuth> call = jcPyAnyApi.auth(requAuth);
        call.enqueue(new Callback<RptaAuth>() {
            @Override
            public void onResponse(@NonNull Call<RptaAuth> call, @NonNull Response<RptaAuth> response) {
                SharedPreferences.Editor editor = sharedPref.edit();
                if (!response.isSuccessful()) {
                    // Si falla el servidor, permitimos acceso con el rol seleccionado explícitamente por el usuario
                    editor.putString("TOKEN_JWT", "fallback_token");
                    editor.putString("USER_ROLE", selectedRole);
                    editor.apply();

                    if (onTokenSaved != null) {
                        onTokenSaved.run();
                    }
                } else {
                    RptaAuth rptaAuth = response.body();
                    if (rptaAuth != null && rptaAuth.getAccess_token() != null) {
                        editor.putString("TOKEN_JWT", rptaAuth.getAccess_token());
                    } else {
                        editor.putString("TOKEN_JWT", "api_token");
                    }
                    // Forzar el rol seleccionado por el usuario en el login
                    editor.putString("USER_ROLE", selectedRole);
                    editor.apply();

                    if (onTokenSaved != null) {
                        onTokenSaved.run();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<RptaAuth> call, @NonNull Throwable throwable) {
                // En caso de fallo de red, usamos el rol seleccionado en el Spinner
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("TOKEN_JWT", "offline_token");
                editor.putString("USER_ROLE", selectedRole);
                editor.apply();

                if (onTokenSaved != null) {
                    onTokenSaved.run();
                }
            }
        });
    }
}
