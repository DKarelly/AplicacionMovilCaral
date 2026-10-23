package com.example.py_damb2026ii;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.py_damb2026ii.Interface.JCPyAnyApi;
import com.example.py_damb2026ii.databinding.ActivityLoginBinding;
import com.example.py_damb2026ii.model.RequAuth;
import com.example.py_damb2026ii.model.RptaAuth;
import com.example.py_damb2026ii.model.RptaObtenerDiscos;

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
        EdgeToEdge.enable(this);

        sharedPref = this.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.nextButton.setOnClickListener(v -> {
            binding.nextButton.setEnabled(false);

            obtenerTokenApi(() -> {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });


    }

    private void obtenerTokenApi(Runnable onTokenSaved){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://jcachay.pythonanywhere.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JCPyAnyApi jcPyAnyApi = retrofit.create(JCPyAnyApi.class);
        RequAuth requAuth = new RequAuth();
        requAuth.setUsername(binding.usernameEditText.getText().toString());
        requAuth.setPassword(binding.passwordEditText.getText().toString());
        Call<RptaAuth> call = jcPyAnyApi.auth(requAuth);
        call.enqueue(new Callback<RptaAuth>() {
            @Override
            public void onResponse(Call<RptaAuth> call, Response<RptaAuth> response) {
                if(!response.isSuccessful()){
                    binding.txtRptaJson.setText("Código: " + response.code());
                }else{
                    RptaAuth rptaAuth = response.body();
                    // binding.txtRptaJson.setText(rptaAuth.getAccess_token());
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("TOKEN_JWT", rptaAuth.getAccess_token());
                    if (rptaAuth != null) {
                        editor.putString("USER_ROLE", rptaAuth.getRol());
                    }
                    editor.apply();
                    if (onTokenSaved != null) {
                        onTokenSaved.run();
                    }
                }
            }

            @Override
            public void onFailure(Call<RptaAuth> call, Throwable throwable) {
                binding.txtRptaJson.setText(throwable.getMessage());
            }
        });
    }

}