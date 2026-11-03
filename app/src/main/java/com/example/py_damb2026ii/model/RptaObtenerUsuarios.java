
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
        private int activo;

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public boolean isActivo() {
            return activo == 1;
        }
    }
}
