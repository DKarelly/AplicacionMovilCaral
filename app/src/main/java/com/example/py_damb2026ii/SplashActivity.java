package com.example.py_damb2026ii;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.py_damb2026ii.databinding.ActivitySplashBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashActivity extends AppCompatActivity {

    private ActivitySplashBinding binding;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<Animator> activeAnimators = new ArrayList<>();
    private Runnable finishRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            Class<?> clazz = Class.forName("androidx.core.splashscreen.SplashScreen");
            clazz.getMethod("installSplashScreen", android.app.Activity.class).invoke(null, this);
        } catch (Exception ignored) {}
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciarAnimacion();
    }

    private void iniciarAnimacion() {
        if (binding == null) return;

        binding.waveView.startWaveAnimation(2200);

        // 35 elementos cayendo (alternando espiga_arroz y logo_molino)
        crearEspigasCayendo(35);

        handler.postDelayed(this::revelarLogoYTextos, 2000);

        finishRunnable = this::navegarADestino;
        handler.postDelayed(finishRunnable, 3500);
    }

    private void crearEspigasCayendo(int cantidad) {
        if (binding == null) return;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        Random random = new Random();

        for (int i = 0; i < cantidad; i++) {
            ImageView espiga = new ImageView(this);
            if (i % 2 == 0) {
                espiga.setImageResource(R.drawable.espiga_arroz);
            } else {
                espiga.setImageResource(R.drawable.logo_molino);
            }
            espiga.setScaleType(ImageView.ScaleType.FIT_CENTER);

            // Tamaño variado (0.5x a 1.3x) de una base de 60dp
            float scale = 0.5f + random.nextFloat() * 0.8f;
            int baseSizePx = (int) (60 * metrics.density);
            int sizePx = Math.max(24, (int) (baseSizePx * scale));

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizePx, sizePx);
            int marginX = random.nextInt(Math.max(1, screenWidth - sizePx - 20)) + 10;
            lp.leftMargin = marginX;
            espiga.setLayoutParams(lp);

            espiga.setTranslationY(-sizePx - 40);

            float startRotation = -40f + random.nextFloat() * 80f;
            espiga.setRotation(startRotation);

            float startAlpha = 0.4f + random.nextFloat() * 0.6f;
            espiga.setAlpha(startAlpha);

            binding.ticketsContainer.addView(espiga);

            long delay = random.nextInt(700);
            long duration = 1300 + random.nextInt(600);

            float targetY = screenHeight * (0.45f + random.nextFloat() * 0.10f);

            ObjectAnimator fallAnim = ObjectAnimator.ofFloat(espiga, View.TRANSLATION_Y, -sizePx - 40, targetY);
            fallAnim.setDuration(duration);
            fallAnim.setStartDelay(delay);
            fallAnim.setInterpolator(new DecelerateInterpolator());

            float endRotation = startRotation + (-25f + random.nextFloat() * 50f);
            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(espiga, View.ROTATION, startRotation, endRotation);
            rotateAnim.setDuration(duration);
            rotateAnim.setStartDelay(delay);

            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(espiga, View.ALPHA, startAlpha, 0.0f);
            fadeAnim.setDuration(450);
            fadeAnim.setStartDelay(delay + duration - 450);

            AnimatorSet set = new AnimatorSet();
            set.playTogether(fallAnim, rotateAnim, fadeAnim);
            activeAnimators.add(set);
            set.start();
        }
    }

    private void revelarLogoYTextos() {
        if (binding == null) return;

        binding.imgLogo.setVisibility(View.VISIBLE);
        binding.txtTitle.setVisibility(View.VISIBLE);
        binding.txtSubtitle.setVisibility(View.VISIBLE);

        binding.imgLogo.setScaleX(0.4f);
        binding.imgLogo.setScaleY(0.4f);
        binding.imgLogo.setAlpha(0.0f);

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_X, 0.4f, 1.0f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_Y, 0.4f, 1.0f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(binding.imgLogo, View.ALPHA, 0.0f, 1.0f);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(logoScaleX, logoScaleY, logoAlpha);
        logoSet.setDuration(700);
        logoSet.setInterpolator(new OvershootInterpolator(1.3f));

        binding.txtTitle.setTranslationY(35f);
        binding.txtTitle.setAlpha(0.0f);
        ObjectAnimator titleTransY = ObjectAnimator.ofFloat(binding.txtTitle, View.TRANSLATION_Y, 35f, 0f);
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(binding.txtTitle, View.ALPHA, 0.0f, 1.0f);

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(titleTransY, titleAlpha);
        titleSet.setDuration(550);
        titleSet.setStartDelay(180);

        binding.txtSubtitle.setTranslationY(25f);
        binding.txtSubtitle.setAlpha(0.0f);
        ObjectAnimator subTransY = ObjectAnimator.ofFloat(binding.txtSubtitle, View.TRANSLATION_Y, 25f, 0f);
        ObjectAnimator subAlpha = ObjectAnimator.ofFloat(binding.txtSubtitle, View.ALPHA, 0.0f, 1.0f);

        AnimatorSet subSet = new AnimatorSet();
        subSet.playTogether(subTransY, subAlpha);
        subSet.setDuration(550);
        subSet.setStartDelay(320);

        AnimatorSet masterSet = new AnimatorSet();
        masterSet.playTogether(logoSet, titleSet, subSet);
        activeAnimators.add(masterSet);
        masterSet.start();
    }

    private void navegarADestino() {
        if (isFinishing() || isDestroyed()) return;

        SharedPreferences prefs = getSharedPreferences("USAT_SP", Context.MODE_PRIVATE);
        String token = prefs.getString("TOKEN_JWT", "");

        if (token != null && !token.trim().isEmpty()) {
            com.example.py_damb2026ii.repository.SupabaseAuthRepository.INSTANCE.restoreSession(
                    this,
                    androidx.lifecycle.LifecycleOwnerKt.getLifecycleScope(this),
                    restored -> {
                        if (isFinishing() || isDestroyed()) return;
                        Intent intent;
                        if (restored) {
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        } else {
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                        }
                        if (getIntent() != null && getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        startActivity(intent);
                        finish();
                    }
            );
        } else {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            if (getIntent() != null && getIntent().getExtras() != null) {
                intent.putExtras(getIntent().getExtras());
            }
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (finishRunnable != null) {
            handler.removeCallbacks(finishRunnable);
        }
        handler.removeCallbacksAndMessages(null);

        for (Animator anim : activeAnimators) {
            if (anim != null) {
                anim.cancel();
            }
        }
        activeAnimators.clear();

        if (binding != null && binding.waveView != null) {
            binding.waveView.stopAnimation();
        }
        binding = null;
    }
}
