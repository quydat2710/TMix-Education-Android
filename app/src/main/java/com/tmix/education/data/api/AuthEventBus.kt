package com.tmix.education.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global event bus for authentication events.
 * When the backend returns 401 (token expired), this emits an event
 * so the UI layer can redirect to the login screen.
 */
object AuthEventBus {

    private val _sessionExpired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1 // buffer 1 event so it's never lost
    )

    /** Observe this flow to react to session expiration */
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    /** Called by the OkHttp interceptor when a 401 is received */
    fun emitSessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
