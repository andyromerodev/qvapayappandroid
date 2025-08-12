package com.example.qvapayappandroid.presentation.ui.p2p.createp2poffer

import com.example.qvapayappandroid.data.model.P2PDetail

/**
 * Moneda disponible para crear ofertas P2P
 */
data class AvailableCoin(
    val id: Int,
    val name: String,
    val tick: String
)

/**
 * Estado específico para la creación de ofertas P2P
 */
data class CreateP2POfferState(
    val isLoading: Boolean = false,
    val type: String = "sell",
    val coinId: String = "108",
    val selectedCoin: AvailableCoin = CreateP2POfferState.availableCoins.first { it.id == 108 },
    val availableCoins: List<AvailableCoin> = CreateP2POfferState.availableCoins,
    val amount: String = "",
    val receive: String = "",
    val details: List<P2PDetail> = listOf(
        P2PDetail("Nombre y Apellidos", ""),
        P2PDetail("Nro de tarjeta", ""),
        P2PDetail("Nro de celular", "")
    ),
    val onlyKyc: Boolean = true,
    val private: Boolean = false,
    val promoteOffer: Boolean = false,
    val onlyVip: Boolean = true,
    val message: String = "",
    val webhook: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isThrottled: Boolean = false,
    val throttleRemainingSeconds: Int = 0
) {
    companion object {
        val availableCoins = listOf(
            AvailableCoin(6, "USDT (TRC20)", "USDT (TRC20)"),
            AvailableCoin(14, "Zelle", "Zelle"),
            AvailableCoin(17, "QvaPay", "QvaPay"),
            AvailableCoin(25, "Solana", "Solana"),
            AvailableCoin(29, "SberBank", "SberBank"),
            AvailableCoin(31, "EUR", "EUR Bank"),
            AvailableCoin(32, "MLC", "MLC"),
            AvailableCoin(34, "CUP", "CUP Bank"),
            AvailableCoin(41, "Saldo ETECSA", "Saldo ETECSA"),
            AvailableCoin(46, "TropiPay", "TropiPay"),
            AvailableCoin(51, "Bolsa TM", "Bolsa TM"),
            AvailableCoin(68, "USDT (BSC)", "USDT (BSC)"),
            AvailableCoin(83, "USD Cash", "USD Cash"),
            AvailableCoin(84, "CUP Cash", "CUP Cash"),
            AvailableCoin(87, "EUR Cash", "EUR Cash"),
            AvailableCoin(89, "NeoMoon", "NeoMoon"),
            AvailableCoin(108, "CLASICA", "CLASICA"),
            AvailableCoin(109, "BANDEC PREPAGO", "BANDEC PREPAGO")
        )
    }
}