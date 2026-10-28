
package com.example.py_damb2026ii.model;

import java.util.List;

public class RptaDetalleIncidente {

    private int code;
    private Datos data;
    private String msge;

    public int getCode() {
        return code;
    }

    public Datos getData() {
        return data;
    }

    public String getMsge() {
        return msge;
    }

    public static class Datos {
        public Incidente incidente;
        public List<CambioEstado> historial;
    }

    public static class Incidente {
        public int id;
        public String titulo;
        public String descripcion;
        public String categoria;
        public String prioridad;
        public String estado;
        public String sucursal;
        public String solicitante;
        public String tecnico;
        public String fecha_registro;
        public Integer id_activo;
    }

    public static class CambioEstado {
        public String estado_anterior;
        public String estado_nuevo;
        public String comentario;
        public String usuario;
        public String fecha_cambio;
    }
}
