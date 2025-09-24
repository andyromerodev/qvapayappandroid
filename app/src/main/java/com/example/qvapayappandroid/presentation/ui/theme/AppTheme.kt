package com.example.qvapayappandroid.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.qvapayappandroid.domain.usecase.GetSettingsUseCase
import org.koin.androidx.compose.get

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val getSettingsUseCase: GetSettingsUseCase = get()
    val systemInDarkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    
    // Use a Flow to observe real-time settings changes
    val settingsFlow = remember { getSettingsUseCase.flow() }
    val settings by settingsFlow.collectAsState(initial = null)
    
    val userTheme = settings?.theme ?: "Sistema"
    
    val darkTheme = when (userTheme) {
        "Oscuro" -> true
        "Claro" -> false
        else -> systemInDarkTheme // "Sistema"
    }
    
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
