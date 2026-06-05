package com.pentagono.rolematch.core.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Base64
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrCodeGenerator {

    fun generate(content: String, size: Int = 512): Bitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 2,
                EncodeHintType.CHARACTER_SET to "UTF-8"
            )
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}

object CouponTokenBuilder {

    fun build(
        couponId: String,
        userId: String,
        restaurantId: String,
        issuedAt: Long = System.currentTimeMillis(),
        expiresAt: Long
    ): String {
        val payload = "$couponId|$userId|$restaurantId|$issuedAt|$expiresAt"
        val hash = payload.hashCode().toString(16).padStart(8, '0')
        val full = "$payload|$hash"
        return Base64.encodeToString(
            full.toByteArray(Charsets.UTF_8),
            Base64.URL_SAFE or Base64.NO_WRAP
        )
    }

    fun parse(token: String): CouponTokenPayload? {
        return try {
            val decoded = String(
                Base64.decode(token, Base64.URL_SAFE),
                Charsets.UTF_8
            )
            val parts = decoded.split("|")
            if (parts.size != 6) return null

            val couponId     = parts[0]
            val userId       = parts[1]
            val restaurantId = parts[2]
            val issuedAt     = parts[3]
            val expiresAt    = parts[4]
            val hash         = parts[5]

            val payload = "$couponId|$userId|$restaurantId|$issuedAt|$expiresAt"
            val expectedHash = payload.hashCode().toString(16).padStart(8, '0')
            if (hash != expectedHash) return null

            CouponTokenPayload(
                couponId = couponId,
                userId = userId,
                restaurantId = restaurantId,
                issuedAt = issuedAt.toLong(),
                expiresAt = expiresAt.toLong()
            )
        } catch (e: Exception) {
            null
        }
    }

    data class CouponTokenPayload(
        val couponId: String,
        val userId: String,
        val restaurantId: String,
        val issuedAt: Long,
        val expiresAt: Long
    ) {
        fun isExpired() = System.currentTimeMillis() > expiresAt
    }
}