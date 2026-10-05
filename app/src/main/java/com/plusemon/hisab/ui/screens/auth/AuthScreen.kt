package com.plusemon.hisab.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
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
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.plusemon.hisab.domain.util.AuthDiagnostics
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
    val clipboardManager = LocalClipboardManager.current
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
    var showDiagnosticDialog by remember { mutableStateOf(false) }

    val currentAppSha1 = remember { AuthDiagnostics.getCurrentAppSha1(context) }
    val isSha1Registered = remember { AuthDiagnostics.isSha1RegisteredInFirebase(context) }

    if (showDiagnosticDialog) {
        AlertDialog(
            onDismissRequest = { showDiagnosticDialog = false },
            icon = {
                Icon(
                    imageVector = if (!isSha1Registered) Icons.Default.Key else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (!isSha1Registered) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = if (isBn) {
                        if (!isSha1Registered) "গুগল সাইন ইন ও SHA-1 কনফিগারেশন" else "গুগল সাইন ইন সহায়তা"
                    } else {
                        if (!isSha1Registered) "Google Sign-In & SHA-1 Setup" else "Google Sign-In Help"
                    },
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (!isSha1Registered) {
                        Text(
                            text = if (isBn)
                                "আপনার ডিভাইসে গুগল অ্যাকাউন্ট থাকা সত্ত্বেও সাইন ইন হচ্ছে না, কারণ বর্তমান অ্যাপটির সাইনিং SHA-1 ফিঙ্গারপ্রিন্ট ফায়ারবেসে (Firebase Console) যুক্ত করা নেই। নিরাপত্তা নিশ্চিত করার জন্য গুগল প্লে সার্ভিসেস এই অনুরোধটি প্রত্যাখ্যান করেছে।"
                            else
                                "Google Sign-In failed although accounts exist on your device because this app's signing SHA-1 certificate is not registered in Firebase Console. Google Play Services enforces this security check.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // SHA-1 Display Card with copy
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (isBn) "বর্তমান অ্যাপের SHA-1 ফিঙ্গারপ্রিন্ট:" else "Current App SHA-1 Fingerprint:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentAppSha1,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(currentAppSha1))
                                        Toast.makeText(
                                            context,
                                            if (isBn) "SHA-1 ক্লিপবোর্ডে কপি করা হয়েছে!" else "SHA-1 copied to clipboard!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBn) "SHA-1 কপি করুন" else "Copy SHA-1",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (isBn)
                                "সমাধান:\n১. Firebase Console > Project Settings এ যান\n২. 'com.plusemon.hisab' অ্যাপের নিচে 'Add fingerprint' এ এই SHA-1 যুক্ত করুন।\n৩. অথবা সরাসরি নিচে ইমেইল ও পাসওয়ার্ড দিয়ে লগইন বা নতুন অ্যাকাউন্ট খুলুন।"
                            else
                                "How to fix:\n1. Open Firebase Console > Project Settings\n2. Under 'com.plusemon.hisab', click 'Add fingerprint' and paste this SHA-1.\n3. Alternatively, sign in or register with Email & Password below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = if (isBn)
                                "গুগল সাইন ইন সম্পন্ন করা সম্ভব হয়নি। অনুগ্রহ করে নিশ্চিত করুন যে আপনার ডিভাইসে ইন্টারনেট সংযোগ সক্রিয় রয়েছে, গুগল প্লে সার্ভিসেস আপডেট করা আছে, অথবা ডিভাইসের সেটিংস থেকে অ্যাকাউন্টটি যাচাই করুন।"
                            else
                                "Google Sign-In could not complete. Please ensure your device has an active internet connection, Google Play Services is updated, or verify accounts in device settings.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiagnosticDialog = false
                    },
                    modifier = Modifier.testTag("auth_dialog_use_email")
                ) {
                    Text(if (isBn) "ইমেইল দিয়ে ব্যবহার করুন" else "Use Email Sign-in")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDiagnosticDialog = false
                        try {
                            val intent = Intent(Settings.ACTION_SYNC_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                context.startActivity(Intent(Settings.ACTION_SETTINGS))
                            } catch (e2: Exception) {
                                // Ignore
                            }
                        }
                    },
                    modifier = Modifier.testTag("auth_dialog_check_settings")
                ) {
                    Text(if (isBn) "ডিভাইস সেটিংস" else "Device Settings")
                }
            }
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 12.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Language Switcher in top corner (reusing Settings language toggle logic)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
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

            // App Branding Hero
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = "Hisab Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
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

                                    fun isCancellation(t: Throwable): Boolean {
                                        var curr: Throwable? = t
                                        while (curr != null) {
                                            if (curr is GetCredentialCancellationException) return true
                                            val msg = curr.message ?: ""
                                            if (msg.contains("cancel", ignoreCase = true) ||
                                                msg.contains("user cancelled", ignoreCase = true)
                                            ) {
                                                return true
                                            }
                                            curr = curr.cause
                                        }
                                        return false
                                    }

                                    try {
                                        var result: GetCredentialResponse? = null

                                        // 1. Primary Attempt: Use GetSignInWithGoogleOption (the Google recommended Button flow
                                        // which opens the full Google Account chooser dialog listing all device accounts)
                                        try {
                                            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = webClientId)
                                                .build()
                                            val request = GetCredentialRequest.Builder()
                                                .addCredentialOption(signInOption)
                                                .build()
                                            result = credentialManager.getCredential(
                                                context = activity,
                                                request = request
                                            )
                                        } catch (e: Throwable) {
                                            if (isCancellation(e)) {
                                                Log.i("GoogleSignIn", "User cancelled Google Sign-In picker.")
                                                return@launch
                                            }
                                            Log.w("GoogleSignIn", "GetSignInWithGoogleOption failed: ${e.message}. Trying GetGoogleIdOption fallback...")
                                        }

                                        // 2. Fallback: Try GetGoogleIdOption with filterByAuthorizedAccounts = false
                                        if (result == null) {
                                            val googleIdOption = GetGoogleIdOption.Builder()
                                                .setFilterByAuthorizedAccounts(false)
                                                .setServerClientId(webClientId)
                                                .setAutoSelectEnabled(false)
                                                .build()
                                            val fallbackRequest = GetCredentialRequest.Builder()
                                                .addCredentialOption(googleIdOption)
                                                .build()
                                            result = credentialManager.getCredential(
                                                context = activity,
                                                request = fallbackRequest
                                            )
                                        }

                                        val finalResult = result
                                            ?: throw NoCredentialException("No credentials returned from Google Sign-In")

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
                                        if (isCancellation(e)) {
                                            Log.i("GoogleSignIn", "Google Sign-In cancelled/dismissed by user.")
                                        } else {
                                            Log.e("GoogleSignIn", "Google Sign-In failed: ${e.message}", e)
                                            showDiagnosticDialog = true
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
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
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

            Spacer(modifier = Modifier.height(32.dp))
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
