package com.pentagono.rolematch.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.pentagono.rolematch.domain.model.Coupon
import com.pentagono.rolematch.domain.model.CouponStatus
import com.pentagono.rolematch.domain.model.UserCoupon
import com.pentagono.rolematch.domain.repository.CouponRepository
import kotlinx.coroutines.tasks.await
import java.util.Date

class FirebaseCouponRepository : CouponRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val coupons = firestore.collection("coupons")
    private val userCoupons = firestore.collection("userCoupons")

    override suspend fun getCouponById(id: String): Result<Coupon> {
        return try {
            val doc = coupons.document(id).get().await()
            val coupon = doc.toCoupon()
                ?: return Result.failure(Exception("Cupom não encontrado"))
            Result.success(coupon)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCouponsByRestaurant(restaurantId: String): Result<List<Coupon>> {
        return try {
            val snapshot = coupons
                .whereEqualTo("restaurantId", restaurantId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { it.toCoupon() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCouponsWithAvailability(): Result<List<Coupon>> {
        return try {
            val snapshot = coupons
                .whereEqualTo("active", true)
                .get()
                .await()
            Result.success(snapshot.documents.mapNotNull { it.toCoupon() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createCoupon(coupon: Coupon): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não autenticado"))

            val data = coupon.toMap(uid)
            val docRef = coupons.document()
            docRef.set(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCoupon(coupon: Coupon): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid
                ?: return Result.failure(Exception("Usuário não autenticado"))
            coupons.document(coupon.id).update(coupon.toMap(uid)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCoupon(couponId: String): Result<Unit> {
        return try {
            coupons.document(couponId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun issueCouponToUser(couponId: String, userId: String): Result<UserCoupon> {
        return try {
            val couponDoc = coupons.document(couponId).get().await()
            val coupon = couponDoc.toCoupon()
                ?: return Result.failure(Exception("Cupom não encontrado"))

            val token = generateQrToken(couponId, userId)
            val userCoupon = hashMapOf(
                "couponId" to couponId,
                "userId" to userId,
                "restaurantId" to coupon.restaurantId,
                "couponTitle" to coupon.title,
                "discountValue" to coupon.discountValue,
                "qrToken" to token,
                "status" to "AVAILABLE",
                "issuedAt" to FieldValue.serverTimestamp(),
                "expiresAt" to coupon.validUntil
            )
            val docRef = userCoupons.document()
            docRef.set(userCoupon).await()

            Result.success(
                UserCoupon(
                    id = docRef.id,
                    couponId = couponId,
                    userId = userId,
                    restaurantId = coupon.restaurantId,
                    couponTitle = coupon.title,
                    discountValue = coupon.discountValue,
                    qrToken = token,
                    status = CouponStatus.AVAILABLE,
                    issuedAt = Date(),
                    expiresAt = coupon.validUntil
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun validateCouponQr(qrToken: String, restaurantId: String): Result<UserCoupon> {
        return try {
            val snapshot = userCoupons.whereEqualTo("qrToken", qrToken).limit(1).get().await()
            val doc = snapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("QR Code inválido"))

            val status = doc.getString("status") ?: "AVAILABLE"
            if (status == "USED") return Result.failure(Exception("Cupom já utilizado"))

            val expiresAt = (doc.getTimestamp("expiresAt"))?.toDate() ?: Date(0)
            if (expiresAt.before(Date())) return Result.failure(Exception("Cupom expirado"))

            val docRestaurantId = doc.getString("restaurantId") ?: ""
            if (docRestaurantId != restaurantId) return Result.failure(Exception("Cupom de outro restaurante"))

            Result.success(
                UserCoupon(
                    id = doc.id,
                    couponId = doc.getString("couponId") ?: "",
                    userId = doc.getString("userId") ?: "",
                    restaurantId = docRestaurantId,
                    couponTitle = doc.getString("couponTitle") ?: "",
                    discountValue = doc.getString("discountValue") ?: "",
                    qrToken = qrToken,
                    status = CouponStatus.AVAILABLE,
                    expiresAt = expiresAt
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markCouponAsUsed(userCouponId: String, operatorId: String): Result<Unit> {
        return try {
            userCoupons.document(userCouponId).update(
                mapOf(
                    "status" to "USED",
                    "usedAt" to FieldValue.serverTimestamp(),
                    "validatedBy" to operatorId
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserCoupons(userId: String): Result<List<UserCoupon>> {
        return try {
            val snapshot = userCoupons
                .whereEqualTo("userId", userId)
                .orderBy("issuedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            val result = snapshot.documents.mapNotNull { doc ->
                try {
                    UserCoupon(
                        id = doc.id,
                        couponId = doc.getString("couponId") ?: "",
                        userId = userId,
                        restaurantId = doc.getString("restaurantId") ?: "",
                        couponTitle = doc.getString("couponTitle") ?: "",
                        discountValue = doc.getString("discountValue") ?: "",
                        qrToken = doc.getString("qrToken") ?: "",
                        status = try {
                            CouponStatus.valueOf(doc.getString("status") ?: "AVAILABLE")
                        } catch (e: Exception) { CouponStatus.AVAILABLE },
                        issuedAt = doc.getTimestamp("issuedAt")?.toDate() ?: Date(),
                        expiresAt = doc.getTimestamp("expiresAt")?.toDate() ?: Date()
                    )
                } catch (e: Exception) { null }
            }
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun com.google.firebase.firestore.DocumentSnapshot.toCoupon(): Coupon? {
        return try {
            Coupon(
                id = id,
                restaurantId = getString("restaurantId") ?: "",
                title = getString("title") ?: "",
                description = getString("description") ?: "",
                benefitType = getString("benefitType") ?: "PERCENTAGE",
                discountValue = getString("discountValue") ?: "",
                rules = getString("rules") ?: "",
                validFrom = getTimestamp("validFrom")?.toDate() ?: Date(),
                validUntil = getTimestamp("validUntil")?.toDate() ?: Date(),
                maxUses = (getLong("maxUses") ?: 100L).toInt(),
                maxUsesPerUser = (getLong("maxUsesPerUser") ?: 1L).toInt(),
                active = getBoolean("active") ?: true,
                status = getString("status") ?: "ACTIVE",
                redeemCount = (getLong("redeemCount") ?: 0L).toInt(),
                internalCode = getString("internalCode") ?: "",
                createdAt = getTimestamp("createdAt")?.toDate() ?: Date()
            )
        } catch (e: Exception) { null }
    }

    private fun Coupon.toMap(ownerId: String): Map<String, Any?> = mapOf(
        "restaurantId" to restaurantId,
        "ownerId" to ownerId,
        "title" to title,
        "description" to description,
        "benefitType" to benefitType,
        "discountValue" to discountValue,
        "rules" to rules,
        "validFrom" to com.google.firebase.Timestamp(validFrom),
        "validUntil" to com.google.firebase.Timestamp(validUntil),
        "maxUses" to maxUses,
        "maxUsesPerUser" to maxUsesPerUser,
        "active" to active,
        "status" to status,
        "redeemCount" to redeemCount,
        "internalCode" to internalCode,
        "createdAt" to FieldValue.serverTimestamp()
    )

    private fun generateQrToken(couponId: String, userId: String): String {
        val payload = "$couponId:$userId:${System.currentTimeMillis()}"
        return android.util.Base64.encodeToString(
            payload.toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
    }
}