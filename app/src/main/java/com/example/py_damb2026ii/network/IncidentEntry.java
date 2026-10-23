package com.example.py_damb2026ii.network;

public class IncidentEntry {
    public final int id;
    public final String titulo;
    public final String descripcion;
    public final String categoria;
    public final String prioridad;
    public final String estado;
    public final int id_sucursal;
    public final String fecha_registro;

    public IncidentEntry(int id, String titulo, String descripcion, String categoria, String prioridad, String estado, int id_sucursal, String fecha_registro) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.prioridad = prioridad;
        this.estado = estado;
        this.id_sucursal = id_sucursal;
        this.fecha_registro = fecha_registro;
    }
}
