package com.example.py_damb2026ii.model;

public class RptaAuth {
    private String access_token;
    private UserInfo user;
    private String rol;
    private String role;

    public static class UserInfo {
        private int id;
        private String username;
        private String nombre;
        private String correo;
        private String rol;
        private int id_sucursal;
        private String sucursal;

        public String getRol() { return rol; }
        public int getIdSucursal() { return id_sucursal; }
        public String getSucursal() { return sucursal; }
    }

    public String getAccess_token() { return access_token; }
    public UserInfo getUser() { return user; }

    public String getRol() {
        if (user != null && user.getRol() != null && !user.getRol().isEmpty()) return user.getRol();
        if (rol != null && !rol.isEmpty()) return rol;
        return role == null || role.isEmpty() ? "Solicitante" : role;
    }

    public String getNombreVisible(String ingreso) {
        if (user != null) {
            if (user.nombre != null && !user.nombre.trim().isEmpty()) return user.nombre.trim();
            if (user.username != null && !user.username.trim().isEmpty()) return user.username.trim();
        }
        return ingreso == null ? "Colaborador" : ingreso;
    }
}
