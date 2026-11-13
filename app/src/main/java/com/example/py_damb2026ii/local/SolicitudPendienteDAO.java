package com.example.py_damb2026ii.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SolicitudPendienteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SolicitudPendienteEntity solicitud);

    @Query("SELECT * FROM solicitudes_pendientes WHERE id_usuario = :idUsuario ORDER BY id ASC")
    List<SolicitudPendienteEntity> getPendientesPorUsuario(String idUsuario);

    @Query("UPDATE solicitudes_pendientes SET estado = :estado WHERE id = :id")
    void actualizarEstado(int id, String estado);

    @Query("UPDATE solicitudes_pendientes SET estado = 'Pendiente' WHERE estado = 'Enviando'")
    void resetearEnviando();

    @Query("DELETE FROM solicitudes_pendientes WHERE id = :id")
    void deleteById(int id);

    @Query("DELETE FROM solicitudes_pendientes WHERE id_usuario = :idUsuario")
    void deleteByUsuario(String idUsuario);
}
