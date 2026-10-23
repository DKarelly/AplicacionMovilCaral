package com.example.py_damb2026ii.network;

import androidx.annotation.NonNull;

public class SucursalEntry {
    public final int id;
    public final String nombre;
    public final String direccion;
    public final double latitud;
    public final double longitud;
    public final Integer id_empresa;

    public SucursalEntry(int id, String nombre, String direccion, double latitud, double longitud, Integer id_empresa) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.id_empresa = id_empresa;
    }

    @NonNull
    @Override
    public String toString() {
        return nombre + " (" + direccion + ")";
    }
}
