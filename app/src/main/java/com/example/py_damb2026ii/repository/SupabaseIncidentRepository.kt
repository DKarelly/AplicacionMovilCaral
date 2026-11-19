package com.example.py_damb2026ii.repository

import android.content.Context
import com.example.py_damb2026ii.network.SupabaseClientProvider
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface IncidentCallback {
    fun onSuccess(incidenteId: String)
    fun onError(message: String)
}

object SupabaseIncidentRepository {

    fun registrarIncidente(
        context: Context,
        titulo: String,
        descripcion: String,
        categoria: String,
        prioridad: String,
        activoId: String?,
        callback: IncidentCallback,
        scope: CoroutineScope
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE)
                val empresaId = prefs.getString("USER_EMPRESA_ID", "")
                val sucursalId = prefs.getString("USER_SUCURSAL_ID", "")

                if (empresaId.isNullOrEmpty() || sucursalId.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback.onError("No se encontró la empresa o sucursal del usuario en la sesión.")
                    }
                    return@launch
                }

                val postgrest = SupabaseClientProvider.client.postgrest

                val params = mapOf(
                    "p_empresa_id" to empresaId,
                    "p_sucursal_id" to sucursalId,
                    "p_titulo" to titulo,
                    "p_descripcion" to descripcion,
                    "p_categoria" to categoria,
                    "p_prioridad" to prioridad,
                    "p_activo_id" to activoId
                )

                val incidenteId = postgrest.rpc("inc_crear_incidente", params).decodeSingle<String>()

                withContext(Dispatchers.Main) {
                    callback.onSuccess(incidenteId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback.onError("Error al registrar incidente: ${e.message}")
                }
            }
        }
    }
}
