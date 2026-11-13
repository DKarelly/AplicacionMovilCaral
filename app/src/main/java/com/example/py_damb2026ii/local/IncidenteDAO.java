package com.example.py_damb2026ii.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface IncidenteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<IncidenteEntity> incidentes);

    @Query("SELECT * FROM incidentes_cache WHERE id_usuario_cache = :idUsuarioCache ORDER BY id DESC")
    List<IncidenteEntity> getIncidentesPorUsuario(String idUsuarioCache);

    @Query("DELETE FROM incidentes_cache WHERE id_usuario_cache = :idUsuarioCache")
    void deleteByUsuario(String idUsuarioCache);

    @Query("DELETE FROM incidentes_cache")
    void deleteAll();
}
