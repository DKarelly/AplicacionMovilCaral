package com.example.py_damb2026ii.model;

public class RptaAuth {
    private String access_token;
    private UserInfo user;
    private String rol;
    private String role;

    public static class UserInfo {
        private int id;
        private String username;
        private String rol;
        private int activo;

        public String getRol() {
            return rol;
        }
    }

    public String getAccess_token() {
        return access_token;
    }

    public String getRol() {
        if (user != null && user.getRol() != null && !user.getRol().isEmpty()) {
            return user.getRol();
        }
        if (rol != null && !rol.isEmpty()) return rol;
        return role != null && !role.isEmpty() ? role : "Solicitante";
    }
}
