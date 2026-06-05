package com.pentagono.rolematch.core.theme

import androidx.compose.ui.graphics.Color

// ── Brand greens ──────────────────────────────────────────────────
val PrimaryGreen    = Color(0xFF5F9F00)
val DarkGreen       = Color(0xFF3F7300)
val MediumGreen     = Color(0xFF7AB800)
val LightGreen      = Color(0xFFE6F5D0)
val SurfaceGreen    = Color(0xFFD9FBE8)

// ── Background & surfaces (light) ────────────────────────────────
val BackgroundCream = Color(0xFFFFF8ED)
val CardWhite       = Color(0xFFFFFFFF)
val CardCream       = Color(0xFFFFF6E6)
val BorderLight     = Color(0xFFE5E7EB)
val DividerLight    = Color(0xFFF3F4F6)

// ── Text (light) ──────────────────────────────────────────────────
val TextDark        = Color(0xFF111827)
val TextMuted       = Color(0xFF6B7280)
val TextLight       = Color(0xFF9CA3AF)
val TextOnPrimary   = Color(0xFFFFFFFF)

// ── Accents ───────────────────────────────────────────────────────
val DiscountRed     = Color(0xFFF0142F)
val OrangeAccent    = Color(0xFFFF6B00)
val BlueAccent      = Color(0xFF004AAD)
val PurpleAccent    = Color(0xFFA83CFF)
val WarningYellow   = Color(0xFFFFE9A3)
val StarYellow      = Color(0xFFFBBF24)

// ── Status colors ─────────────────────────────────────────────────
val StatusOpen      = Color(0xFF22C55E)
val StatusOpenBg    = Color(0xFFDCFCE7)
val StatusClosed    = Color(0xFFEF4444)
val StatusClosedBg  = Color(0xFFFEE2E2)
val StatusPending   = Color(0xFFF59E0B)
val StatusPendingBg = Color(0xFFFEF3C7)

// ── Dark theme ────────────────────────────────────────────────────
// Regra: cada camada é levemente mais clara que a anterior
val DarkBackground    = Color(0xFF121212)  // fundo — não preto puro
val DarkSurface       = Color(0xFF1E1E1E)  // cards — um tom acima do fundo
val DarkCard          = Color(0xFF2A2A2A)  // cards elevados
val DarkBorder        = Color(0xFF3A3A3A)  // bordas sutis
val DarkTextPrimary   = Color(0xFFE8E8E8)  // texto — branco suavizado
val DarkTextSecondary = Color(0xFFB0B0B0)  // texto secundário

// Dark: cores de marca adaptadas
val DarkPrimaryGreen  = Color(0xFF7AB800)  // verde levemente mais brilhante no escuro
val DarkLightGreen    = Color(0xFF1E3A00)  // substitui LightGreen no escuro

// Dark: status adaptados para fundo escuro
val DarkStatusOpenBg   = Color(0xFF0D3320)
val DarkStatusClosedBg = Color(0xFF3D0A0A)
val DarkStatusPendingBg = Color(0xFF3D2800)

// Dark: erro e card de erro
val DarkErrorBg       = Color(0xFF3D0A0A)