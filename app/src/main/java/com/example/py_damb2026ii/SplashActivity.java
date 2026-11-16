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
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        iniciarAnimacion();
    }

    private void iniciarAnimacion() {
        binding.waveView.startWaveAnimation(2200);

        crearTicketsCayendo(10);

        handler.postDelayed(this::revelarLogoYTextos, 2100);

        finishRunnable = this::navegarADestino;
        handler.postDelayed(finishRunnable, 3500);
    }

    private void crearTicketsCayendo(int cantidad) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        Random random = new Random();

        for (int i = 0; i < cantidad; i++) {
            ImageView ticket = new ImageView(this);
            ticket.setImageResource(R.drawable.ic_incident_logo);

            int sizeDp = 32 + random.nextInt(20);
            int sizePx = (int) (sizeDp * metrics.density);

            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(sizePx, sizePx);
            int marginX = random.nextInt(Math.max(1, screenWidth - sizePx - 40)) + 20;
            lp.leftMargin = marginX;
            ticket.setLayoutParams(lp);

            ticket.setTranslationY(-sizePx - 50);
            float startRotation = -30f + random.nextFloat() * 60f;
            ticket.setRotation(startRotation);

            binding.ticketsContainer.addView(ticket);

            long delay = random.nextInt(700);
            long duration = 1200 + random.nextInt(500);

            float targetY = screenHeight * (0.35f + random.nextFloat() * 0.25f);

            ObjectAnimator fallAnim = ObjectAnimator.ofFloat(ticket, View.TRANSLATION_Y, -sizePx - 50, targetY);
            fallAnim.setDuration(duration);
            fallAnim.setStartDelay(delay);
            fallAnim.setInterpolator(new OvershootInterpolator(0.8f));

            ObjectAnimator rotateAnim = ObjectAnimator.ofFloat(ticket, View.ROTATION, startRotation, startRotation + (-20f + random.nextFloat() * 40f));
            rotateAnim.setDuration(duration);
            rotateAnim.setStartDelay(delay);

            ObjectAnimator fadeAnim = ObjectAnimator.ofFloat(ticket, View.ALPHA, 1.0f, 0.0f);
            fadeAnim.setDuration(400);
            fadeAnim.setStartDelay(delay + duration - 300);

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

        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_X, 0.6f, 1.0f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_Y, 0.6f, 1.0f);
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(binding.imgLogo, View.ALPHA, 0.0f, 1.0f);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(logoScaleX, logoScaleY, logoAlpha);
        logoSet.setDuration(700);
        logoSet.setInterpolator(new OvershootInterpolator(1.2f));

        binding.txtTitle.setTranslationY(40f);
        ObjectAnimator titleTransY = ObjectAnimator.ofFloat(binding.txtTitle, View.TRANSLATION_Y, 40f, 0f);
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(binding.txtTitle, View.ALPHA, 0.0f, 1.0f);

        AnimatorSet titleSet = new AnimatorSet();
        titleSet.playTogether(titleTransY, titleAlpha);
        titleSet.setDuration(600);
        titleSet.setStartDelay(200);

        binding.txtSubtitle.setTranslationY(30f);
        ObjectAnimator subTransY = ObjectAnimator.ofFloat(binding.txtSubtitle, View.TRANSLATION_Y, 30f, 0f);
        ObjectAnimator subAlpha = ObjectAnimator.ofFloat(binding.txtSubtitle, View.ALPHA, 0.0f, 1.0f);

        AnimatorSet subSet = new AnimatorSet();
        subSet.playTogether(subTransY, subAlpha);
        subSet.setDuration(600);
        subSet.setStartDelay(350);

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
