package com.plusemon.hisab.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.plusemon.hisab.domain.util.Localization
import com.plusemon.hisab.ui.viewmodel.HisabViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun AuthScreen(
    viewModel: HisabViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val settings by viewModel.settings.collectAsState()
    val isBn = settings.language == "bn"
    val isLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()

    var isSignUp by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showNoAccountDialog by remember { mutableStateOf(false) }

    if (showNoAccountDialog) {
        AlertDialog(
            onDismissRequest = { showNoAccountDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isBn) "গুগল অ্যাকাউন্ট পাওয়া যায়নি" else "No Google Account on Device",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isBn) "এই ডিভাইসে কোনো গুগল অ্যাকাউন্ট সাইন ইন করা নেই। আপনি ডিভাইসের সেটিংসে গিয়ে অ্যাকাউন্ট যোগ করতে পারেন, অথবা নিচে ইমেইল ও পাসওয়ার্ড দিয়ে সহজে প্রবেশ/নিবন্ধন করতে পারেন।"
                    else "No Google account is signed in on this device or emulator. You can add a Google account in device Settings, or sign in / register using Email & Password below."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNoAccountDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_ADD_ACCOUNT).apply {
                                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_SYNC_SETTINGS))
                            } catch (e2: Exception) {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            }
                        }
                    },
                    modifier = Modifier.testTag("no_account_dialog_add")
                ) {
                    Text(if (isBn) "সেটিংস থেকে যোগ করুন" else "Add in Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showNoAccountDialog = false
                    },
                    modifier = Modifier.testTag("no_account_dialog_email")
                ) {
                    Text(if (isBn) "ইমেইল দিয়ে ব্যবহার করুন" else "Use Email Sign-in")
                }
            }
        )
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Language Switcher in top corner (reusing Settings language toggle logic)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    onClick = { viewModel.toggleLanguage() },
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("auth_language_toggle_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Language,
                            contentDescription = "Switch Language",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "বাংলা" else "EN",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 52.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Branding Hero
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = "Hisab Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Localization.getString(Localization.Key.APP_NAME, isBn),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = Localization.getString(Localization.Key.TAGLINE, isBn),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
                )

                // Primary Google Sign In Action (Prominent)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    val activity = context as? Activity
                                    if (activity == null) {
                                        viewModel.handleGoogleSignInFailure(IllegalStateException("Context must be an Activity"))
                                        return@launch
                                    }

                                    val credentialManager = CredentialManager.create(context)
                                    val webClientId = "990037686252-4ma0sd2m5hmihe6802aqm4qo0vfpauue.apps.googleusercontent.com"

                                    suspend fun fetchGoogleCredential(filterByAuthorized: Boolean): GetCredentialResponse {
                                        val googleIdOption = GetGoogleIdOption.Builder()
                                            .setFilterByAuthorizedAccounts(filterByAuthorized)
                                            .setServerClientId(webClientId)
                                            .setAutoSelectEnabled(filterByAuthorized)
                                            .build()

                                        val request = GetCredentialRequest.Builder()
                                            .addCredentialOption(googleIdOption)
                                            .build()

                                        return credentialManager.getCredential(
                                            context = activity,
                                            request = request
                                        )
                                    }

                                    fun isNoCredential(t: Throwable): Boolean {
                                        var curr: Throwable? = t
                                        while (curr != null) {
                                            if (curr is NoCredentialException) return true
                                            val msg = curr.message ?: ""
                                            if (msg.contains("No credentials available", ignoreCase = true) ||
                                                msg.contains("NoCredentialException", ignoreCase = true) ||
                                                msg.contains("No credentials found", ignoreCase = true)
                                            ) {
                                                return true
                                            }
                                            curr = curr.cause
                                        }
                                        return false
                                    }

                                    try {
                                        var result: GetCredentialResponse? = null
                                        var firstAttemptNoCredential = false

                                        // 1. First attempt: filterByAuthorizedAccounts = true for fast/silent sign-in on returning users
                                        try {
                                            result = fetchGoogleCredential(filterByAuthorized = true)
                                        } catch (e: Throwable) {
                                            if (isNoCredential(e)) {
                                                Log.d("GoogleSignIn", "No previously authorized accounts. Retrying once with filterByAuthorizedAccounts = false for full account picker.")
                                                firstAttemptNoCredential = true
                                            } else {
                                                throw e
                                            }
                                        }

                                        // 2. If first attempt throws NoCredentialException, automatically retry ONCE with filterByAuthorizedAccounts = false
                                        if (result == null && firstAttemptNoCredential) {
                                            result = fetchGoogleCredential(filterByAuthorized = false)
                                        }

                                        val finalResult = result
                                            ?: throw NoCredentialException("No credentials available")

                                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(finalResult.credential.data)
                                        val idToken = googleIdTokenCredential.idToken
                                        val userEmail = googleIdTokenCredential.id
                                        val userDisplayName = googleIdTokenCredential.displayName
                                            ?: googleIdTokenCredential.givenName
                                            ?: userEmail.substringBefore("@")
                                        val userPhotoUrl = googleIdTokenCredential.profilePictureUri?.toString()
                                        val googleId = googleIdTokenCredential.id

                                        // Optionally authenticate with Firebase Auth
                                        val firebaseUid = signInFirebaseWithGoogleToken(context, idToken)

                                        viewModel.signInWithGoogle(
                                            uid = firebaseUid ?: googleId,
                                            email = userEmail,
                                            name = userDisplayName,
                                            photoUrl = userPhotoUrl
                                        )
                                    } catch (e: Throwable) {
                                        val isCancellation = e is GetCredentialCancellationException ||
                                                e.message?.contains("cancel", ignoreCase = true) == true

                                        if (isCancellation) {
                                            Log.i("GoogleSignIn", "Google Sign-In cancelled/dismissed by user.")
                                        } else if (isNoCredential(e)) {
                                            // 3. Only show "No Google Account on Device" if BOTH attempts fail (truly no account on device)
                                            Log.w("GoogleSignIn", "Both attempts failed with NoCredentialException: No Google account found on device.")
                                            showNoAccountDialog = true
                                            viewModel.handleGoogleSignInFailure(e)
                                        } else {
                                            Log.e("GoogleSignIn", "Google Sign-In error: ${e.message}", e)
                                            viewModel.handleGoogleSignInFailure(e)
                                        }
                                    }
                                }
                            },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("google_signin_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Stylized Google 'G' icon badge
                            Text(
                                text = "G",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = Color(0xFF4285F4)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = Localization.getString(Localization.Key.SIGN_IN_WITH_GOOGLE, isBn),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f))
                        Text(
                            text = if (isBn) " অথবা " else " or ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In / Sign Up Tab
                    TabRow(
                        selectedTabIndex = if (isSignUp) 1 else 0,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .padding(2.dp)
                    ) {
                        Tab(
                            selected = !isSignUp,
                            onClick = {
                                isSignUp = false
                                viewModel.clearAuthError()
                            },
                            text = {
                                Text(
                                    text = if (isBn) "লগইন" else "Sign In",
                                    fontWeight = if (!isSignUp) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_signin")
                        )
                        Tab(
                            selected = isSignUp,
                            onClick = {
                                isSignUp = true
                                viewModel.clearAuthError()
                            },
                            text = {
                                Text(
                                    text = if (isBn) "নিবন্ধন" else "Sign Up",
                                    fontWeight = if (isSignUp) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_signup")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Error Message Banner
                    AnimatedVisibility(visible = authError != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = authError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    // Fields
                    if (isSignUp) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                viewModel.clearAuthError()
                            },
                            label = { Text(Localization.getString(Localization.Key.NAME, isBn)) },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .testTag("auth_name_input")
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            viewModel.clearAuthError()
                        },
                        label = { Text(Localization.getString(Localization.Key.EMAIL, isBn)) },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("auth_email_input")
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.clearAuthError()
                        },
                        label = { Text(Localization.getString(Localization.Key.PASSWORD, isBn)) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (isSignUp) {
                                    viewModel.signUpWithEmail(name, email, password)
                                } else {
                                    viewModel.signInWithEmail(email, password)
                                }
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                            .testTag("auth_password_input")
                    )

                    Button(
                        onClick = {
                            if (isSignUp) {
                                viewModel.signUpWithEmail(name, email, password)
                            } else {
                                viewModel.signInWithEmail(email, password)
                            }
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = if (isSignUp) Localization.getString(Localization.Key.CREATE_ACCOUNT, isBn)
                                else Localization.getString(Localization.Key.SIGN_IN_EMAIL, isBn),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
}

private suspend fun signInFirebaseWithGoogleToken(context: Context, idToken: String): String? {
    return suspendCancellableCoroutine { continuation ->
        try {
            if (com.google.firebase.FirebaseApp.getApps(context).isEmpty()) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            auth.signInWithCredential(credential)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (continuation.isActive) {
                        continuation.resume(uid)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("GoogleSignIn", "Firebase Auth sign-in failed: ${e.message}")
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        } catch (e: Exception) {
            Log.w("GoogleSignIn", "Firebase Auth exception: ${e.message}")
            if (continuation.isActive) {
                continuation.resume(null)
            }
        }
    }
}
