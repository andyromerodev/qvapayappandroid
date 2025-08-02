package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.domain.usecase.LoginUseCase
import org.koin.dsl.module

val domainModule = module {
    // Use Cases
    factory { LoginUseCase(get()) }
}