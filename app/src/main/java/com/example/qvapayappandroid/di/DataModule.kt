package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.datasource.LoginDataSource
import com.example.qvapayappandroid.data.datasource.LoginDataSourceImpl
import com.example.qvapayappandroid.data.datasource.OfferTemplateLocalDataSource
import com.example.qvapayappandroid.data.datasource.OfferTemplateLocalDataSourceImpl
import com.example.qvapayappandroid.data.datasource.P2PDataSource
import com.example.qvapayappandroid.data.datasource.P2PDataSourceImpl
import com.example.qvapayappandroid.data.datasource.WebViewLoginDataSource
import com.example.qvapayappandroid.data.datasource.WebViewLoginDataSourceImpl
import com.example.qvapayappandroid.data.datastore.SessionPreferencesRepository
import com.example.qvapayappandroid.data.permissions.NotificationPermissionManager
import com.example.qvapayappandroid.data.repository.AuthRepositoryImpl
import com.example.qvapayappandroid.data.repository.OfferAlertRepositoryImpl
import com.example.qvapayappandroid.data.repository.OfferTemplateRepositoryImpl
import com.example.qvapayappandroid.data.repository.P2PRepositoryImpl
import com.example.qvapayappandroid.data.repository.SessionRepositoryDataStoreImpl
import com.example.qvapayappandroid.data.repository.SettingsRepositoryDataStoreImpl
import com.example.qvapayappandroid.data.migration.SessionDataMigration
import com.example.qvapayappandroid.data.migration.SettingsDataMigration
import com.example.qvapayappandroid.data.repository.WebViewRepositoryImpl
import com.example.qvapayappandroid.data.throttling.ThrottlingManagerImpl
import com.example.qvapayappandroid.data.work.OfferAlertWorkManager
import com.example.qvapayappandroid.domain.repository.AuthRepository
import com.example.qvapayappandroid.domain.repository.OfferAlertRepository
import com.example.qvapayappandroid.domain.repository.OfferTemplateRepository
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
    
    // DataSources (Optimized - removed obsolete Session and Settings DataSources)
    single<LoginDataSource> { LoginDataSourceImpl(get()) }
    single<P2PDataSource> { P2PDataSourceImpl(get(), get()) }
    single<OfferTemplateLocalDataSource> { OfferTemplateLocalDataSourceImpl(get()) }

    // WebView DataSource
    single<WebViewLoginDataSource> { WebViewLoginDataSourceImpl() }

    // Permissions Manager
    single { NotificationPermissionManager(androidContext()) }

    // Work Manager
    single { OfferAlertWorkManager(androidContext()) }
    
    // Migration
    single { SessionDataMigration(get(), get(), get()) }
    single { SettingsDataMigration(get(), get()) }

    // Repositories (Optimized for DataStore)
    single<SessionRepository> { SessionRepositoryDataStoreImpl(get(), get()) }
    single<AuthRepository> { AuthRepositoryImpl(get<LoginDataSource>(), get<SessionRepository>()) }
    single<SettingsRepository> { SettingsRepositoryDataStoreImpl(get()) }
    single<P2PRepository> { P2PRepositoryImpl(get(), get()) }
    single<WebViewRepository> { WebViewRepositoryImpl(get()) }
    single<OfferTemplateRepository> { OfferTemplateRepositoryImpl(get()) }
    single<OfferAlertRepository> { OfferAlertRepositoryImpl(get()) }
}