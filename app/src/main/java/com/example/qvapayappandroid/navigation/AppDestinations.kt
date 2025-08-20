package com.example.qvapayappandroid.navigation

sealed class AppDestinations(val route: String) {
    object Splash : AppDestinations("splash")
    object Login : AppDestinations("login")
    object Main : AppDestinations("main")
    object Home : AppDestinations("home")
    object P2P : AppDestinations("p2p")
    object P2POfferDetail : AppDestinations("p2p_offer_detail/{offerId}") {
        fun createRoute(offerId: String) = "p2p_offer_detail/$offerId"
    }
    object MyOfferDetail : AppDestinations("my_offer_detail/{offerId}") {
        fun createRoute(offerId: String) = "my_offer_detail/$offerId"
    }
    object P2PFilters : AppDestinations("p2p_filters")
    object CreateP2POffer : AppDestinations("create_p2p_offer")
    object Templates : AppDestinations("templates")
    object SaveOfferTemplate : AppDestinations("save_offer_template") 
    object EditOfferTemplate : AppDestinations("edit_offer_template/{templateId}") {
        fun createRoute(templateId: Long) = "edit_offer_template/$templateId"
    }
    object WebView : AppDestinations("webview")
    object P2PWebView : AppDestinations("p2p_webview/{offerId}") {
        fun createRoute(offerId: String) = "p2p_webview/$offerId"
    }
    object Settings : AppDestinations("settings")
    object OfferAlerts : AppDestinations("offer_alerts")
    object CreateOfferAlert : AppDestinations("create_offer_alert")
    object EditOfferAlert : AppDestinations("edit_offer_alert/{alertId}") {
        fun createRoute(alertId: Long) = "edit_offer_alert/$alertId"
    }
    object UserProfile : AppDestinations("user_profile")
    
    companion object {
        const val START_DESTINATION = "splash"
    }
}