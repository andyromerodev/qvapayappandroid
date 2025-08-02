package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.datasource.LoginDataSourceImpl
import com.example.qvapayappandroid.data.repository.AuthRepositoryImpl
import com.example.qvapayappandroid.domain.repository.AuthRepository
import org.koin.dsl.module

val dataModule = module {
    // DataSources
    single<LoginDataSource> { LoginDataSourceImpl(get()) }
    
    // Repositories
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}