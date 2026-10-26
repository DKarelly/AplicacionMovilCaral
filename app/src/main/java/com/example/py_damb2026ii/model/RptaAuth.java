package com.example.py_damb2026ii.model;

public class RptaAuth {
    private String access_token;
    private UserAuthModel user;

    public String getAccess_token() {
        return access_token;
    }

    public UserAuthModel getUser() {
        return user;
    }

    public String getRol() {
        if (user != null && user.getRol() != null) {
            return user.getRol();
        }
        return "Solicitante";
    }
}
