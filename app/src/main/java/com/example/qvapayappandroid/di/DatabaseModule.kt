package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.database.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().sessionDao() }
    single { get<AppDatabase>().settingsDao() }
    single { get<AppDatabase>().offerTemplateDao() }
    single { get<AppDatabase>().offerAlertDao() }
    single { get<AppDatabase>().p2pOfferDao() }
}