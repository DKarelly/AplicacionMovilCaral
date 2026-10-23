package com.example.py_damb2026ii.model;

public class RequInsertarSucursal {
    private String nombre;
    private String direccion;
    private double latitud;
    private double longitud;
    private Integer id_empresa;

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setId_empresa(Integer id_empresa) {
        this.id_empresa = id_empresa;
    }
}
