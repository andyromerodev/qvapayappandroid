package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.datasource.LoginDataSourceImpl
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.datasource.P2PDataSourceImpl
import com.example.qvapayappandroid.data.datasource.SessionLocalDataSource
import com.example.qvapayappandroid.data.datasource.SessionLocalDataSourceImpl
import com.example.qvapayappandroid.data.repository.AuthRepositoryImpl
import com.example.qvapayappandroid.data.repository.SessionRepositoryImpl
import com.example.qvapayappandroid.domain.repository.AuthRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository
import org.koin.dsl.module

val dataModule = module {
    // DataSources
    single<LoginDataSource> { LoginDataSourceImpl(get()) }
    single<SessionLocalDataSource> { SessionLocalDataSourceImpl(get(), get()) }
    single<P2PDataSource> { P2PDataSourceImpl(get(), get()) }
    
    // Repositories
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
}