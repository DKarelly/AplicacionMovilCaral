package com.example.py_damb2026ii.model;

public class RptaObtenerActivoQR {
    private int code;
    private String msge;
    private ActivoQR data;

    public static class ActivoQR {
        public int id;
        public String codigo_qr;
        public String nombre;
        public String tipo;
        public String estado;
        public int id_sucursal;
        public String nombre_sucursal;
    }

    public int getCode() {
        return code;
    }

    public String getMsge() {
        return msge;
    }

    public ActivoQR getData() {
        return data;
    }
}
