package com.example.py_damb2026ii.model;

public class RequInsertarIncidente {
    private String titulo;
    private String descripcion;
    private String categoria;
    private String prioridad;
    private int id_sucursal;
    private Integer id_activo;

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setPrioridad(String prioridad) {
        this.prioridad = prioridad;
    }

    public void setId_sucursal(int id_sucursal) {
        this.id_sucursal = id_sucursal;
    }

    public void setId_activo(Integer id_activo) {
        this.id_activo = id_activo;
    }
}
