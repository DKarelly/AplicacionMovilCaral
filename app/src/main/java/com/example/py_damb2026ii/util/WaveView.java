package com.example.py_damb2026ii.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class WaveView extends View {

    private final Paint paintWave1 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintWave2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintWave3 = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Path pathWave1 = new Path();
    private final Path pathWave2 = new Path();
    private final Path pathWave3 = new Path();

    private float phase1 = 0f;
    private float phase2 = 0f;
    private float phase3 = 0f;

    private float waveRiseProgress = 0f;

    private ValueAnimator phaseAnimator;
    private ValueAnimator riseAnimator;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paintWave1.setColor(Color.parseColor("#58FFFFFF"));
        paintWave1.setStyle(Paint.Style.FILL);

        paintWave2.setColor(Color.parseColor("#994E7D9A"));
        paintWave2.setStyle(Paint.Style.FILL);

        paintWave3.setColor(Color.parseColor("#D9284D65"));
        paintWave3.setStyle(Paint.Style.FILL);
    }

    public void startWaveAnimation(long durationMs) {
        stopAnimation();

        phaseAnimator = ValueAnimator.ofFloat(0f, (float) (2 * Math.PI));
        phaseAnimator.setDuration(1800);
        phaseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        phaseAnimator.setInterpolator(new LinearInterpolator());
        phaseAnimator.addUpdateListener(animator -> {
            float val = (float) animator.getAnimatedValue();
            phase1 = val;
            phase2 = val * 1.5f;
            phase3 = val * 0.8f;
            invalidate();
        });

        riseAnimator = ValueAnimator.ofFloat(0f, 0.95f);
        riseAnimator.setDuration(durationMs);
        riseAnimator.addUpdateListener(animator -> {
            waveRiseProgress = (float) animator.getAnimatedValue();
            invalidate();
        });

        phaseAnimator.start();
        riseAnimator.start();
    }

    public void stopAnimation() {
        if (phaseAnimator != null) {
            phaseAnimator.cancel();
            phaseAnimator = null;
        }
        if (riseAnimator != null) {
            riseAnimator.cancel();
            riseAnimator = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (width == 0 || height == 0) return;

        float currentY = height * (1.0f - waveRiseProgress);

        float waveAmplitude1 = height * 0.03f;
        float waveAmplitude2 = height * 0.04f;
        float waveAmplitude3 = height * 0.025f;

        buildWavePath(pathWave1, width, height, currentY, waveAmplitude1, phase1, 1.0f);
        canvas.drawPath(pathWave1, paintWave1);

        buildWavePath(pathWave2, width, height, currentY + height * 0.02f, waveAmplitude2, phase2 + 1.5f, 1.2f);
        canvas.drawPath(pathWave2, paintWave2);

        buildWavePath(pathWave3, width, height, currentY + height * 0.035f, waveAmplitude3, phase3 + 3.0f, 0.9f);
        canvas.drawPath(pathWave3, paintWave3);
    }

    private void buildWavePath(Path path, int width, int height, float baseY, float amplitude, float phase, float frequency) {
        path.reset();
        path.moveTo(0, height);

        for (int x = 0; x <= width; x += 10) {
            float y = (float) (baseY + amplitude * Math.sin((2 * Math.PI * x / width * frequency) + phase));
            path.lineTo(x, y);
        }

        path.lineTo(width, height);
        path.close();
    }
}
