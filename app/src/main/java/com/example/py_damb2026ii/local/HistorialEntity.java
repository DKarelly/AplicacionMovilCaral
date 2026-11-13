package com.example.py_damb2026ii.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "historial_cache")
public class HistorialEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int id_incidente;
    public String estado_anterior;
    public String estado_nuevo;
    public String comentario;
    public String usuario;
    public String fecha_cambio;
    public String id_usuario_cache;
}
