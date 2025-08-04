package com.example.qvapayappandroid.data.network

object ApiConfig {
    const val BASE_URL = "https://qvapay.com/api"
    
    object Endpoints {
        const val AUTH_LOGIN = "/auth/login"
        const val AUTH_LOGOUT = "/auth/logout"
        const val AUTH_REGISTER = "/auth/register"
        const val USER_PROFILE = "/user/profile"
        const val TRANSACTIONS = "/transactions"
        const val BALANCE = "/balance"
        const val P2P_INDEX = "/p2p/index"
        const val P2P_OFFER = "/p2p"
    }
}