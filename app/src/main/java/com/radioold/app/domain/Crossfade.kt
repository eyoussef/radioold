package com.radioold.app.domain

/**
 * Crossfade volume curve. Pure math so the transition feel is unit-testable.
 * Audio transitions use a smooth fade at track boundaries (Radiooooo-style),
 * ramped from these curves by the player controller.
 */
object Crossfade {
    const val DEFAULT_MS = 3000L

    /** Volume (0..1) at elapsed time [tMs] into a fade-in over [fadeMs]. */
    fun fadeIn(tMs: Long, fadeMs: Long = DEFAULT_MS): Float {
        if (fadeMs <= 0L) return 1f
        return (tMs.toFloat() / fadeMs).coerceIn(0f, 1f)
    }

    /** Volume (0..1) with [remainingMs] left, during a fade-out over [fadeMs]. */
    fun fadeOut(remainingMs: Long, fadeMs: Long = DEFAULT_MS): Float {
        if (fadeMs <= 0L) return 1f
        return (remainingMs.toFloat() / fadeMs).coerceIn(0f, 1f)
    }
}
