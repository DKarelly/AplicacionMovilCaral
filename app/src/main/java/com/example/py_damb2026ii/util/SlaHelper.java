package com.example.py_damb2026ii.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/*
 * Semáforo SLA (Módulo 3).
 * Alta = 4 h, Media = 24 h, Baja = 72 h.
 * VERDE: dentro del tiempo · AMARILLO: queda menos del 25 % · ROJO: vencido.
 */
public class SlaHelper {

    public static final int SLA_VERDE = 1;
    public static final int SLA_AMARILLO = 2;
    public static final int SLA_ROJO = 3;
    public static final int SLA_FINALIZADO = 4;

    public static final int COLOR_VERDE = 0xFF2E7D32;
    public static final int COLOR_AMARILLO = 0xFFF9A825;
    public static final int COLOR_ROJO = 0xFFC62828;
    public static final int COLOR_NARANJA = 0xFFEF6C00;
    public static final int COLOR_GRIS = 0xFF757575;

    /* Horas límite según la prioridad. */
    public static int horasLimite(String prioridad) {
        if ("Alta".equalsIgnoreCase(prioridad)) return 4;
        if ("Media".equalsIgnoreCase(prioridad)) return 24;
        if ("Baja".equalsIgnoreCase(prioridad)) return 72;
        return 24;
    }

    /* Calcula el estado del semáforo. */
    public static int calcularSla(String prioridad, String estado, long fechaRegistro, long ahora) {
        if ("Resuelto".equalsIgnoreCase(estado) || "Cerrado".equalsIgnoreCase(estado)) {
            return SLA_FINALIZADO;
        }
        long limiteMs = horasLimite(prioridad) * 60L * 60L * 1000L;
        long transcurridoMs = ahora - fechaRegistro;
        long restanteMs = limiteMs - transcurridoMs;

        if (restanteMs < 0) {
            return SLA_ROJO;
        } else if (restanteMs < limiteMs * 0.25) {
            return SLA_AMARILLO;
        } else {
            return SLA_VERDE;
        }
    }

    /* Color de la etiqueta del semáforo. */
    public static int colorSla(int sla) {
        switch (sla) {
            case SLA_VERDE: return COLOR_VERDE;
            case SLA_AMARILLO: return COLOR_AMARILLO;
            case SLA_ROJO: return COLOR_ROJO;
            default: return COLOR_GRIS;
        }
    }

    /* Texto de la etiqueta, "SLA: quedan 2 h 15 min". */
    public static String textoSla(int sla, String prioridad, long fechaRegistro, long ahora) {
        if (sla == SLA_FINALIZADO) return "SLA: finalizado";
        long limiteMs = horasLimite(prioridad) * 60L * 60L * 1000L;
        long restanteMs = limiteMs - (ahora - fechaRegistro);
        if (restanteMs < 0) {
            return "SLA vencido hace " + formatearDuracion(-restanteMs);
        }
        return "SLA: quedan " + formatearDuracion(restanteMs);
    }

    /* Milisegundos a texto: "3 d 4 h", "2 h 15 min" o "40 min". */
    public static String formatearDuracion(long ms) {
        long minutosTotales = ms / 60000L;
        long dias = minutosTotales / (60 * 24);
        long horas = (minutosTotales / 60) % 24;
        long minutos = minutosTotales % 60;
        if (dias > 0) return dias + " d " + horas + " h";
        if (horas > 0) return horas + " h " + minutos + " min";
        return minutos + " min";
    }

    /* Color de prioridad: rojo = Alta, naranja = Media, verde = Baja. */
    public static int colorPrioridad(String prioridad) {
        if ("Alta".equalsIgnoreCase(prioridad)) return COLOR_ROJO;
        if ("Media".equalsIgnoreCase(prioridad)) return COLOR_NARANJA;
        if ("Baja".equalsIgnoreCase(prioridad)) return COLOR_VERDE;
        return COLOR_GRIS;
    }

    /*
     * Convierte la fecha de la API a milisegundos.
     * Flask envía: "Tue, 27 Oct 2026 10:24:00 GMT"; el JSON local: "2026-10-23 10:30:00".
     * Devuelve -1 si no se puede leer.
     */
    public static long parsearFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return -1;
        try {
            SimpleDateFormat formatoFlask = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
            Date d = formatoFlask.parse(fecha);
            if (d != null) return d.getTime();
        } catch (ParseException ignored) { }
        try {
            SimpleDateFormat formatoMysql = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date d = formatoMysql.parse(fecha);
            if (d != null) return d.getTime();
        } catch (ParseException ignored) { }
        return -1;
    }

    /* Fecha legible: "27/10/2026 10:24". */
    public static String formatearFecha(long millis) {
        SimpleDateFormat salida = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        salida.setTimeZone(TimeZone.getDefault());
        return salida.format(new Date(millis));
    }
}