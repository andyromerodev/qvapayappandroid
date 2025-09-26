package com.example.qvapayappandroid.presentation.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import kotlinx.coroutines.delay
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    val uiState by viewModel.state.collectAsState()
    val screenVisibilityState = remember {
        MutableTransitionState(false).apply { targetState = true }
    }
    var pendingNavigation by remember { mutableStateOf(false) }
    val updatedOnLoginSuccess by rememberUpdatedState(onLoginSuccess)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> {
                    pendingNavigation = true
                    screenVisibilityState.targetState = false
                }
                is LoginEffect.ShowSuccessMessage -> {
                    // Success messages can be handled here with snackbar or similar
                    Log.d("LoginScreen", "Success: ${effect.message}")
                }
                is LoginEffect.ShowErrorMessage -> {
                    // Error messages can be handled here with snackbar or similar
                    Log.e("LoginScreen", "Error: ${effect.message}")
                }
            }
        }
    }

    LaunchedEffect(screenVisibilityState.currentState, screenVisibilityState.targetState, pendingNavigation) {
        val isHidden = !screenVisibilityState.currentState && !screenVisibilityState.targetState
        if (pendingNavigation && isHidden) {
            pendingNavigation = false
            updatedOnLoginSuccess()
        }
    }

    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF716EC5))
    ) {
        AnimatedVisibility(
            visibleState = screenVisibilityState,
            enter = fadeIn(animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)) +
                slideInVertically(
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 5 }
                ),
            exit = fadeOut(animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing)) +
                slideOutVertically(
                    animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                    targetOffsetY = { it / 6 }
                )
        ) {
            LoginForm(
                uiState = uiState,
                viewModel = viewModel,
                focusManager = focusManager,
                passwordVisible = passwordVisible,
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible }
            )
        }
    }
}

@Composable
private fun LoginForm(
    uiState: LoginState,
    viewModel: LoginViewModel,
    focusManager: androidx.compose.ui.focus.FocusManager,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo/Brand Section
        LoginHeader()
        
        AnimatedField(delayMillis = 80) {
            EmailField(
                value = uiState.email,
                onValueChange = { viewModel.handleIntent(LoginIntent.UpdateEmail(it)) },
                isEnabled = !uiState.isLoading,
                focusManager = focusManager
            )
        }

        AnimatedField(delayMillis = 160) {
            PasswordField(
                value = uiState.password,
                onValueChange = { viewModel.handleIntent(LoginIntent.UpdatePassword(it)) },
                isEnabled = !uiState.isLoading,
                isVisible = passwordVisible,
                onVisibilityToggle = onPasswordVisibilityToggle,
                focusManager = focusManager
            )
        }

        AnimatedField(delayMillis = 240) {
            CodeField(
                value = uiState.code,
                onValueChange = { viewModel.handleIntent(LoginIntent.UpdateCode(it)) },
                isEnabled = !uiState.isLoading,
                focusManager = focusManager,
                onDone = { 
                    focusManager.clearFocus()
                    viewModel.handleIntent(LoginIntent.Login)
                }
            )
        }

        // Login Buttons
        LoginButtons(
            uiState = uiState,
            onLogin = {
                focusManager.clearFocus()
                viewModel.handleIntent(LoginIntent.Login)
            }
        )

        // Error Message
        ErrorMessage(uiState.errorMessage) {
            viewModel.handleIntent(LoginIntent.ClearError)
        }

        // Success Message
        SuccessMessage(uiState)
    }
}

@Composable
private fun LoginHeader() {
    Card(
        modifier = Modifier
            .size(120.dp)
            .padding(bottom = 32.dp),
        shape = RoundedCornerShape(60.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "QP",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }

    Text(
        text = "Bienvenido a QvaPay",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Text(
        text = "Inicia sesión para continuar",
        style = MaterialTheme.typography.bodyLarge,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 40.dp)
    )
}

@Composable
private fun AnimatedField(
    delayMillis: Int,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                initialOffsetY = { it / 6 }
            )
    ) {
        content()
    }
}

@Composable
private fun EmailField(
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Correo electrónico") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "Email",
                tint = Color.White
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        )
    )
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Contraseña") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Password",
                tint = Color.White
            )
        },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (isVisible) "Ocultar contraseña" else "Mostrar contraseña",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        )
    )
}

@Composable
private fun CodeField(
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager,
    onDone: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Código 2FA") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "2FA Code",
                tint = Color.White
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        enabled = isEnabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color.White.copy(alpha = 0.6f),
            focusedLabelColor = Color.White,
            unfocusedLabelColor = Color.White.copy(alpha = 0.8f),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = Color.White.copy(alpha = 0.1f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.05f)
        )
    )
}

@Composable
private fun LoginButtons(
    uiState: LoginState,
    onLogin: () -> Unit,
) {
    // Login Button
    Button(
        onClick = onLogin,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = uiState.isLoginEnabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.5f),
            contentColor = Color(0xFF716EC5),
            disabledContentColor = Color(0xFF716EC5).copy(alpha = 0.7f)
        )
    ) {
        if (uiState.isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF716EC5),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Iniciando sesión...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Text(
                text = "Iniciar Sesión",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    errorMessage: String?,
    onClearError: () -> Unit
) {
    errorMessage?.let { error ->
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun SuccessMessage(uiState: LoginState) {
    if (uiState.isLoginSuccessful) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color.Green,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "¡Inicio de sesión exitoso!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF716EC5),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Bienvenido, ${uiState.userDisplayName}",
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF716EC5).copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Balance: $${uiState.userBalance}",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color.Green,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
