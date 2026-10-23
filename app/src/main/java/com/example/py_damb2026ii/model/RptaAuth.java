package com.example.py_damb2026ii.model;

public class RptaAuth {
    private String access_token;
    private String rol;
    private String role;

    public String getAccess_token() {
        return access_token;
    }

    public String getRol() {
        if (rol != null) return rol;
        return role != null ? role : "Solicitante";
    }
}
