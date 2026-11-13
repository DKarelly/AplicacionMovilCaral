package com.example.py_damb2026ii.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "incidentes_cache")
public class IncidenteEntity {
    @PrimaryKey
    public int id;
    public String titulo;
    public String descripcion;
    public String categoria;
    public String prioridad;
    public String estado;
    public int id_sucursal;
    public String fecha_registro;
    public String tecnico;
    public String solicitante;
    public String sucursal;
    public String id_usuario_cache;
}
