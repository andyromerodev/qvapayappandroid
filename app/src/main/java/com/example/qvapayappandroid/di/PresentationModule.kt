package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.presentation.ui.home.HomeViewModel
import com.example.qvapayappandroid.presentation.ui.login.LoginViewModel
import com.example.qvapayappandroid.presentation.ui.main.MainViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.CreateP2POfferViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.P2POfferDetailViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.P2PViewModel
import com.example.qvapayappandroid.presentation.ui.p2p.P2PWebViewViewModel
import com.example.qvapayappandroid.presentation.ui.profile.UserProfileViewModel
import com.example.qvapayappandroid.presentation.ui.settings.SettingsViewModel
import com.example.qvapayappandroid.presentation.ui.splash.SplashViewModel
import com.example.qvapayappandroid.presentation.ui.webview.WebViewFullScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    // ViewModels
    viewModel { SplashViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { UserProfileViewModel(get(), get()) }
    viewModel { P2PViewModel(get()) }
    viewModel { P2POfferDetailViewModel(get()) }
    viewModel { CreateP2POfferViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    viewModel { WebViewFullScreenViewModel() }
    viewModel { P2PWebViewViewModel() }
}