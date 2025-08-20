package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.domain.usecase.ApplyToP2POfferUseCase
import com.example.qvapayappandroid.domain.usecase.ApplyToP2POfferWebViewUseCase
import com.example.qvapayappandroid.domain.usecase.CheckNotificationPermissionUseCase
import com.example.qvapayappandroid.domain.usecase.CheckSessionUseCase
import com.example.qvapayappandroid.domain.usecase.CreateP2POfferUseCase
import com.example.qvapayappandroid.domain.usecase.CancelP2POfferUseCase
import com.example.qvapayappandroid.domain.usecase.DeleteOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.DeleteOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.GetCurrentUserUseCase
import com.example.qvapayappandroid.domain.usecase.GetMyP2POffersUseCase
import com.example.qvapayappandroid.domain.usecase.GetNotificationPermissionStatusUseCase
import com.example.qvapayappandroid.domain.usecase.GetOfferAlertsUseCase
import com.example.qvapayappandroid.domain.usecase.GetOfferTemplatesUseCase
import com.example.qvapayappandroid.domain.usecase.GetP2POfferByIdUseCase
import com.example.qvapayappandroid.domain.usecase.GetP2POffersUseCase
import com.example.qvapayappandroid.domain.usecase.GetSettingsUseCase
import com.example.qvapayappandroid.domain.usecase.InitializeSettingsUseCase
import com.example.qvapayappandroid.domain.usecase.LoadOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.LoginUseCase
import com.example.qvapayappandroid.domain.usecase.LogoutUseCase
import com.example.qvapayappandroid.domain.usecase.ManageAlertWorkManagerUseCase
import com.example.qvapayappandroid.domain.usecase.SaveOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.SaveOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.ToggleOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateBiometricUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateNotificationsUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateOfferAlertUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateOfferTemplateUseCase
import com.example.qvapayappandroid.domain.usecase.UpdateThemeUseCase
import org.koin.dsl.module

val domainModule = module {
    // Use Cases
    factory { LoginUseCase(get()) }
    factory { CheckSessionUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetP2POffersUseCase(get(), get()) }
    factory { GetP2POfferByIdUseCase(get(), get()) }
    factory { ApplyToP2POfferUseCase(get(), get()) }
    factory { CreateP2POfferUseCase(get(), get()) }
    factory { CancelP2POfferUseCase(get(), get()) }
    factory { GetMyP2POffersUseCase(get(), get()) }
    factory { GetSettingsUseCase(get()) }
    factory { InitializeSettingsUseCase(get()) }
    factory { UpdateThemeUseCase(get()) }
    factory { UpdateNotificationsUseCase(get()) }
    factory { UpdateBiometricUseCase(get()) }

    // WebView Use Case
    factory { ApplyToP2POfferWebViewUseCase(get()) }
    
    // Offer Template Use Cases
    factory { SaveOfferTemplateUseCase(get()) }
    factory { GetOfferTemplatesUseCase(get()) }
    factory { DeleteOfferTemplateUseCase(get()) }
    factory { LoadOfferTemplateUseCase(get()) }
    factory { UpdateOfferTemplateUseCase(get()) }
    
    // Offer Alert Use Cases
    factory { GetOfferAlertsUseCase(get()) }
    factory { SaveOfferAlertUseCase(get()) }
    factory { UpdateOfferAlertUseCase(get()) }
    factory { DeleteOfferAlertUseCase(get()) }
    factory { ToggleOfferAlertUseCase(get()) }
    factory { ManageAlertWorkManagerUseCase(get(), get()) }
    
    // Notification Permission Use Cases
    factory { GetNotificationPermissionStatusUseCase(get()) }
    factory { CheckNotificationPermissionUseCase(get()) }
}