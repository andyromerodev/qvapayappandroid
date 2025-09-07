package com.example.qvapayappandroid.presentation.ui.profile

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember

/**
 * Data class representing a snackbar message with its properties
 */
data class SnackbarMessage(
    val text: String,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = true,
    val isError: Boolean = false
)

/**
 * Controller for managing snackbar messages following Clean Architecture principles.
 * This component handles the display logic for snackbars and can be tested independently.
 */
class SnackbarController(
    private val snackbarHostState: SnackbarHostState
) {
    /**
     * Shows a snackbar message with the provided configuration
     */
    suspend fun showMessage(message: SnackbarMessage): SnackbarResult {
        return snackbarHostState.showSnackbar(
            message = message.text,
            actionLabel = message.actionLabel,
            withDismissAction = message.withDismissAction
        )
    }

    /**
     * Shows a success message with default success styling
     */
    suspend fun showSuccess(message: String): SnackbarResult {
        return showMessage(
            SnackbarMessage(
                text = message,
                isError = false
            )
        )
    }

    /**
     * Shows an error message with default error styling
     */
    suspend fun showError(message: String): SnackbarResult {
        return showMessage(
            SnackbarMessage(
                text = message,
                isError = true
            )
        )
    }
}

/**
 * Composable helper function that creates and remembers a SnackbarController
 */
@Composable
fun rememberSnackbarController(): Pair<SnackbarController, SnackbarHostState> {
    val snackbarHostState = remember { SnackbarHostState() }
    val controller = remember { SnackbarController(snackbarHostState) }
    return Pair(controller, snackbarHostState)
}

/**
 * Effect composable that handles UserProfileEffects and displays appropriate snackbars
 */
@Composable
fun SnackbarEffectHandler(
    effect: UserProfileEffect?,
    snackbarController: SnackbarController,
    onEffectConsumed: () -> Unit = {}
) {
    LaunchedEffect(effect) {
        when (effect) {
            is UserProfileEffect.ShowSuccessMessage -> {
                snackbarController.showSuccess(effect.message)
                onEffectConsumed()
            }
            is UserProfileEffect.ShowErrorMessage -> {
                snackbarController.showError(effect.message)
                onEffectConsumed()
            }
            null -> { /* No effect to handle */ }
            else -> { /* Other effects handled elsewhere */ }
        }
    }
}