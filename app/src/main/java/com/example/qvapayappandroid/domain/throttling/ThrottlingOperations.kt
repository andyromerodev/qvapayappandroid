package com.example.qvapayappandroid.domain.throttling

/**
 * Constantes para identificar operaciones de throttling en toda la app
 * 
 * Siguiendo el principio DRY (Don't Repeat Yourself) y facilitando el mantenimiento
 */
object ThrottlingOperations {
    
    // P2P Operations
    const val P2P_GET_OFFERS = "p2p_get_offers"
    const val P2P_GET_OFFER_BY_ID = "p2p_get_offer_by_id"
    const val P2P_CREATE_OFFER = "p2p_create_offer"
    const val P2P_APPLY_TO_OFFER = "p2p_apply_to_offer"
    const val P2P_CANCEL_OFFER = "p2p_cancel_offer"
    const val P2P_GET_MY_OFFERS = "p2p_get_my_offers"
    
    // Auth Operations
    const val AUTH_LOGIN = "auth_login"
    const val AUTH_LOGOUT = "auth_logout"
    const val AUTH_REFRESH_TOKEN = "auth_refresh_token"
    
    // User Operations
    const val USER_GET_PROFILE = "user_get_profile"
    const val USER_UPDATE_PROFILE = "user_update_profile"
    
    // WebView Operations
    const val WEBVIEW_LOAD_URL = "webview_load_url"
    const val WEBVIEW_EXTRACT_DATA = "webview_extract_data"
    
    // General API Operations
    const val API_GENERIC_GET = "api_generic_get"
    const val API_GENERIC_POST = "api_generic_post"
    const val API_GENERIC_PUT = "api_generic_put"
    const val API_GENERIC_DELETE = "api_generic_delete"
}