package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.domain.usecase.CheckSessionUseCase
import com.example.qvapayappandroid.domain.usecase.GetCurrentUserUseCase
import com.example.qvapayappandroid.domain.usecase.GetP2POffersUseCase
import com.example.qvapayappandroid.domain.usecase.LoginUseCase
import com.example.qvapayappandroid.domain.usecase.LogoutUseCase
import org.koin.dsl.module

val domainModule = module {
    // Use Cases
    factory { LoginUseCase(get()) }
    factory { CheckSessionUseCase(get()) }
    factory { GetCurrentUserUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { GetP2POffersUseCase(get(), get()) }
}