package za.co.skinnercafe.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import za.co.skinnercafe.app.data.api.CustomerDto
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun SettingsScreen(
    customer: CustomerDto?,
    appPreferences: AppPreferences,
    isLoading: Boolean,
    successMessage: String?,
    errorMessage: String?,
    hasPassword: Boolean,
    isGoogleLinked: Boolean,
    onLinkGoogleAccount: () -> Unit,
    onSetPassword: (String) -> Unit,
    onChangePassword: (String, String) -> Unit,
    onUnlinkGoogleAccount: (String) -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {

    val isDarkTheme by appPreferences.isDarkTheme.collectAsState(
        initial = false
    )

    val notificationsEnabled by appPreferences.notificationsEnabled.collectAsState(
        initial = true
    )

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var unlinkPassword by remember { mutableStateOf("") }
    var showUnlinkConfirmation by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
        )
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasNotificationPermission =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val notificationsAreActive =
        notificationsEnabled && hasNotificationPermission

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            hasNotificationPermission = isGranted

            coroutineScope.launch {
                appPreferences.setNotificationsEnabled(isGranted)
            }
        }

    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("← Back")
            }

            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.onBackground
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Manage your Skinner Cafe experience.",
                color = colors.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            SettingsSection(title = "MY ACCOUNT") {

                Text(
                    text = "${customer?.firstName.orEmpty()} ${customer?.lastName.orEmpty()}",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = customer?.email ?: "No account information available",
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = customer?.phoneNumber ?: "No phone number added",
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Password",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = if (hasPassword) {
                        "Your account has a password."
                    } else {
                        "Your account currently uses Google sign-in."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                if (hasPassword) {

                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = {
                        Text(if (hasPassword) "New password" else "Create password")
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm new password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (hasPassword) {
                            onChangePassword(currentPassword, newPassword)
                        } else {
                            onSetPassword(newPassword)
                        }

                        currentPassword = ""
                        newPassword = ""
                        confirmPassword = ""
                    },
                    enabled = !isLoading &&
                            customer != null &&
                            newPassword.length >= 8 &&
                            newPassword.any { it.isDigit() } &&
                            newPassword.any { it.isLowerCase() } &&
                            newPassword.any { it.isUpperCase() } &&
                            newPassword.any { !it.isLetterOrDigit() } &&
                            newPassword == confirmPassword &&
                            (!hasPassword || currentPassword.isNotBlank()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        when {
                            isLoading -> "Please wait..."
                            hasPassword -> "Change Password"
                            else -> "Set Password"
                        }
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text = "Google Account",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = if (isGoogleLinked) {
                        "Your Google account is linked."
                    } else {
                        "Your Google account is not linked."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(10.dp))

                if (!isGoogleLinked) {

                    Button(
                        onClick = onLinkGoogleAccount,
                        enabled = !isLoading && customer != null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Link Google Account")
                    }

                } else {

                    Button(
                        onClick = {
                            showUnlinkConfirmation = true
                        },
                        enabled = !isLoading && hasPassword,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.error
                        )
                    ) {
                        Text("Unlink Google Account")
                    }

                    if (!hasPassword) {
                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = "Create a password before unlinking Google. This ensures you won't lose access to your account.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant
                        )
                    }
                }

                if (!successMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = successMessage,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (showUnlinkConfirmation) {
                AlertDialog(
                    onDismissRequest = {
                        showUnlinkConfirmation = false
                        unlinkPassword = ""
                    },
                    title = {
                        Text("Unlink Google account?")
                    },
                    text = {
                        Column {
                            Text(
                                "You will no longer be able to sign in using Google. " +
                                        "You can still sign in using your email and password."
                            )

                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = unlinkPassword,
                                onValueChange = { unlinkPassword = it },
                                label = { Text("Current password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showUnlinkConfirmation = false
                                onUnlinkGoogleAccount(unlinkPassword)
                                unlinkPassword = ""
                            },
                            enabled = !isLoading && unlinkPassword.isNotBlank()
                        ) {
                            Text("Unlink")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showUnlinkConfirmation = false
                                unlinkPassword = ""
                            },
                            enabled = !isLoading
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Spacer(Modifier.height(18.dp))

            SettingsSection(title = "PREFERENCES") {
                PreferenceSwitch(
                    title = "Dark appearance",
                    description = if (isDarkTheme) {
                        "Dark mode is enabled"
                    } else {
                        "Light mode is enabled"
                    },
                    checked = isDarkTheme,
                    onCheckedChange = { enabled ->
                        coroutineScope.launch {
                            appPreferences.setDarkTheme(enabled)
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                PreferenceSwitch(
                    title = "Notifications",
                    description = "Enable or disable app notifications",
                    checked = notificationsAreActive,
                    onCheckedChange = { enabled ->
                        if (
                            enabled &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            coroutineScope.launch {
                                appPreferences.setNotificationsEnabled(enabled)
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Language",
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )

                Text(
                    text = "English",
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(18.dp))

            SettingsSection(title = "SUPPORT") {
                Text(
                    text = "Privacy policy and terms of service will be available here.",
                    color = colors.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "For assistance, please contact Skinner Cafe.",
                    color = colors.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(28.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.error
                )
            ) {
                Text("Sign out")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(14.dp))

            content()
        }
    }
}

@Composable
private fun PreferenceSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}


