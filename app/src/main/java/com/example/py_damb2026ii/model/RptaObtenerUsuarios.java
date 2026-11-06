package com.example.py_damb2026ii.model;

import java.util.List;

public class RptaObtenerUsuarios {

    private int code;
    private List<Usuario> data;
    private String msge;

    public int getCode() {
        return code;
    }

    public List<Usuario> getData() {
        return data;
    }

    public String getMsge() {
        return msge;
    }

    public static class Usuario {

        private int id;
        private String nombre;
        private String correo;
        private String rol;
        private Object activo;
        private Integer id_sucursal;
        private String nombre_sucursal;

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getCorreo() {
            return correo;
        }

        public String getRol() {
            return rol;
        }

        public Integer getIdSucursal() {
            return id_sucursal;
        }

        public String getNombreSucursal() {
            return nombre_sucursal;
        }

        public boolean isActivo() {

            if (activo instanceof Boolean) {
                return (Boolean) activo;
            }

            if (activo instanceof Number) {
                return ((Number) activo).intValue() == 1;
            }

            return activo != null &&
                    ("1".equals(String.valueOf(activo))
                            || "true".equalsIgnoreCase(String.valueOf(activo)));
        }
    }
}