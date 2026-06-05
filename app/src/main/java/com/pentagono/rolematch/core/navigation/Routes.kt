package com.pentagono.rolematch.core.navigation

object Routes {
    // Auth
    const val SPLASH                   = "splash"
    const val LOGIN                    = "login"
    const val REGISTER_USER            = "register_user"
    const val REGISTER_RESTAURANT      = "register_restaurant"
    const val VERIFY_CODE              = "verify_code/{accountType}"
    const val FORGOT_PASSWORD          = "forgot_password"
    const val GOOGLE_COMPLETE_PROFILE  = "google_complete_profile"
    const val RESTAURANT_EDIT           = "restaurant_edit"
    const val RESTAURANT_RESERVATIONS    = "restaurant_reservations"

    // User module
    const val USER_HOME           = "user_home"
    const val SEARCH              = "search"
    const val MOST_FAMOUS         = "most_famous"
    const val CROWD_FAVORITES     = "crowd_favorites"
    const val NEARBY              = "nearby"
    const val BEST_FOR_EVENTS     = "best_for_events"

    const val MATCH               = "match"
    const val MATCH_RESULTS       = "match_results"

    const val RESTAURANT_DETAIL   = "restaurant_detail/{restaurantId}"
    const val RESERVATION         = "reservation/{restaurantId}"

    const val COUPONS             = "coupons"
    const val COUPON_DETAILS      = "coupon_details/{couponId}"
    const val COUPON_QR           = "coupon_qr/{userCouponId}"

    const val USER_PROFILE        = "user_profile"
    const val USER_SETTINGS       = "user_settings"
    const val NOTIFICATIONS       = "notifications"
    const val CHANGE_PASSWORD     = "change_password"

    const val ONBOARDING = "onboarding"

    const val HELP_CENTER         = "help_center"
    const val TERMS_OF_USE        = "terms_of_use"
    const val PRIVACY_POLICY      = "privacy_policy"

    // Restaurant module
    const val RESTAURANT_SETUP         = "restaurant_setup"
    const val RESTAURANT_HOME          = "restaurant_home"
    const val RESTAURANT_PREVIEW       = "restaurant_preview"
    const val RESTAURANT_DASHBOARD     = "restaurant_dashboard"
    const val RESTAURANT_COUPONS       = "restaurant_coupons"
    const val RESTAURANT_COUPON_FORM   = "restaurant_coupon_form"
    const val RESTAURANT_QR_VALIDATOR  = "restaurant_qr_validator"
    const val RESTAURANT_PROFILE       = "restaurant_profile"

    // Admin module
    const val ADMIN_DASHBOARD     = "admin_dashboard"
    const val ADMIN_USERS         = "admin_users"
    const val ADMIN_RESTAURANTS   = "admin_restaurants"
    const val ADMIN_COUPONS       = "admin_coupons"
    const val ADMIN_REPORTS       = "admin_reports"

    // Builder helpers
    fun restaurantDetail(id: String) = "restaurant_detail/$id"
    fun reservation(restaurantId: String) = "reservation/$restaurantId"
    fun couponDetails(couponId: String) = "coupon_details/$couponId"
    fun couponQr(userCouponId: String) = "coupon_qr/$userCouponId"
    fun verifyCode(accountType: String) = "verify_code/$accountType"
}