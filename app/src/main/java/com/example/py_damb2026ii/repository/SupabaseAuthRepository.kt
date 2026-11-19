package com.example.py_damb2026ii.repository

import android.content.Context
import com.example.py_damb2026ii.network.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MobileUser(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val empresa_id: String? = null,
    val sucursal_id: String? = null,
    val activo: Boolean,
    @SerialName("puede_gestionar_incidentes")
    val puedeGestionarIncidentes: Boolean = false,
    @SerialName("areas_autorizadas")
    val areasAutorizadas: List<String> = emptyList()
) {
    fun isPuedeGestionarIncidentes(): Boolean = puedeGestionarIncidentes
    fun getAreasAutorizadasList(): List<String> = areasAutorizadas
}

@Serializable
data class MobileMembership(
    val empresa_id: String,
    val sucursal_id: String? = null
)

interface AuthCallback {
    fun onSuccess(
        user: MobileUser,
        memberships: List<MobileMembership>,
        accessToken: String,
        refreshToken: String
    )

    fun onError(message: String)
}

interface RestoreSessionCallback {
    fun onResult(restored: Boolean)
}

object SupabaseAuthRepository {

    fun login(usuario: String, clave: String, callback: AuthCallback, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                val auth = SupabaseClientProvider.client.auth
                val postgrest = SupabaseClientProvider.client.postgrest

                auth.signInWith(Email) {
                    email = usuario
                    password = clave
                }

                val session = auth.currentSessionOrNull()
                val userId = session?.user?.id

                if (session == null || userId == null) {
                    withContext(Dispatchers.Main) {
                        callback.onError("No se pudo validar tu sesión de autenticación.")
                    }
                    return@launch
                }

                // 1. Intentar obtener perfil mediante la función RPC del ERP inc_perfil_usuario_actual()
                var perfil: MobileUser? = null
                try {
                    perfil = postgrest.rpc("inc_perfil_usuario_actual").decodeSingleOrNull<MobileUser>()
                } catch (e: Exception) {
                    // Fallback a consulta directa de inc_usuarios si la RPC no está disponible aún
                }

                // 2. Si la RPC falló o no retornó perfil, consultar inc_usuarios directamente
                if (perfil == null) {
                    val userResult = postgrest["inc_usuarios"].select {
                        filter {
                            eq("id", userId)
                        }
                    }.decodeList<MobileUser>()
                    perfil = userResult.firstOrNull()
                }

                if (perfil == null || !perfil.activo) {
                    auth.signOut()

                    withContext(Dispatchers.Main) {
                        callback.onError("Cuenta no habilitada para IncidentApp o inactiva.")
                    }

                    return@launch
                }

                val membershipsResult = postgrest["inc_admin_empresa_membresias"].select {
                    filter {
                        eq("usuario_id", userId)
                        eq("activo", true)
                    }
                }.decodeList<MobileMembership>()

                withContext(Dispatchers.Main) {
                    callback.onSuccess(
                        perfil,
                        membershipsResult,
                        session.accessToken,
                        session.refreshToken
                    )
                }

            } catch (e: RestException) {
                e.printStackTrace()
                val msg = e.error ?: e.message ?: "Credenciales inválidas o error de red"
                val finalMsg = if (msg.contains("Invalid login credentials") || e.response.status.value == 400) {
                    "Correo o contraseña incorrectos. Inténtalo nuevamente."
                } else {
                    "Error del servidor: $msg"
                }
                withContext(Dispatchers.Main) {
                    callback.onError(finalMsg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback.onError("No se pudo iniciar sesión. Verifica tu conexión a internet.")
                }
            }
        }
    }

    fun restoreSession(context: Context, scope: CoroutineScope, callback: RestoreSessionCallback) {
        scope.launch(Dispatchers.IO) {
            try {
                val prefs = context.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE)
                val refreshToken = prefs.getString("REFRESH_TOKEN", "")
                val accessToken = prefs.getString("TOKEN_JWT", "")

                if (refreshToken.isNullOrEmpty() && accessToken.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        callback.onResult(false)
                    }
                    return@launch
                }

                val auth = SupabaseClientProvider.client.auth

                if (!refreshToken.isNullOrEmpty()) {
                    auth.refreshSession(refreshToken)
                } else if (!accessToken.isNullOrEmpty()) {
                    auth.importAuthToken(accessToken)
                }

                val currentSession = auth.currentSessionOrNull()
                if (currentSession != null) {
                    prefs.edit()
                        .putString("TOKEN_JWT", currentSession.accessToken)
                        .putString("REFRESH_TOKEN", currentSession.refreshToken)
                        .apply()

                    withContext(Dispatchers.Main) {
                        callback.onResult(true)
                    }
                } else {
                    prefs.edit().clear().apply()
                    withContext(Dispatchers.Main) {
                        callback.onResult(false)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val prefs = context.getSharedPreferences("USAT_SP", Context.MODE_PRIVATE)
                val hasToken = !prefs.getString("TOKEN_JWT", "").isNullOrEmpty()
                withContext(Dispatchers.Main) {
                    callback.onResult(hasToken)
                }
            }
        }
    }

    fun logout() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                SupabaseClientProvider.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
