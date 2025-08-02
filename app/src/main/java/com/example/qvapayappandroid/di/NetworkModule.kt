package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.data.network.HttpClientFactory
import io.ktor.client.*
import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> { HttpClientFactory.create() }
}