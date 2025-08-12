package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.datasource.LoginDataSourceImpl
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.datasource.P2PDataSourceImpl
import com.example.qvapayappandroid.data.datasource.SessionLocalDataSource
import com.example.qvapayappandroid.data.datasource.SessionLocalDataSourceImpl
import com.example.qvapayappandroid.data.datasource.SettingsLocalDataSource
import com.example.qvapayappandroid.data.datasource.SettingsLocalDataSourceImpl
import com.example.qvapayappandroid.data.datasource.WebViewLoginDataSource
import com.example.qvapayappandroid.data.datasource.WebViewLoginDataSourceImpl
import com.example.qvapayappandroid.data.repository.AuthRepositoryImpl
import com.example.qvapayappandroid.data.repository.P2PRepositoryImpl
import com.example.qvapayappandroid.data.repository.SessionRepositoryImpl
import com.example.qvapayappandroid.data.repository.SettingsRepositoryImpl
import com.example.qvapayappandroid.data.repository.WebViewRepositoryImpl
import com.example.qvapayappandroid.data.throttling.ThrottlingManagerImpl
import com.example.qvapayappandroid.domain.repository.AuthRepository
import com.example.qvapayappandroid.domain.repository.P2PRepository
import com.example.qvapayappandroid.domain.repository.SessionRepository
import com.example.qvapayappandroid.domain.repository.SettingsRepository
import com.example.qvapayappandroid.domain.repository.WebViewRepository
import com.example.qvapayappandroid.domain.throttling.ThrottlingManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule = module {
    // Throttling Manager
    single<ThrottlingManager> { ThrottlingManagerImpl() }
    
    // DataSources
    single<LoginDataSource> { LoginDataSourceImpl(get()) }
    single<SessionLocalDataSource> { SessionLocalDataSourceImpl(get(), get()) }
    single<P2PDataSource> { P2PDataSourceImpl(get(), get()) }
    single<SettingsLocalDataSource> { SettingsLocalDataSourceImpl(get()) }

    // WebView DataSource
    single<WebViewLoginDataSource> { WebViewLoginDataSourceImpl() }

    // Repositories
    single<SessionRepository> { SessionRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<P2PRepository> { P2PRepositoryImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<WebViewRepository> { WebViewRepositoryImpl(get()) }
}