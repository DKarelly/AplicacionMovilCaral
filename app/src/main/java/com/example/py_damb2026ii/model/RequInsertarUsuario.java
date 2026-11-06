package com.example.py_damb2026ii.model;

public class RequInsertarUsuario {

    private String nombre;
    private String correo;
    private String password;
    private String rol;
    private int id_sucursal;

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public void setId_sucursal(int id_sucursal) {
        this.id_sucursal = id_sucursal;
    }
}