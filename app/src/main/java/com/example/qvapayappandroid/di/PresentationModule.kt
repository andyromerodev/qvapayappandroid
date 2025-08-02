package com.example.qvapayappandroid.di

import com.example.qvapayappandroid.presentation.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module {
    // ViewModels
    viewModel { LoginViewModel(get()) }
}