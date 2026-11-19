package za.co.skinnercafe.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.material3.Button
import za.co.skinnercafe.app.data.api.CustomerDto
import za.co.skinnercafe.app.ui.auth.AuthUiState
import za.co.skinnercafe.app.ui.auth.AuthViewModel
import za.co.skinnercafe.app.ui.auth.LoginScreen
import za.co.skinnercafe.app.ui.auth.SignUpScreen
import za.co.skinnercafe.app.ui.auth.AuthViewModelFactory
import za.co.skinnercafe.app.ui.settings.AppPreferences
import za.co.skinnercafe.app.ui.settings.SettingsScreen
import za.co.skinnercafe.app.ui.orders.MyOrdersScreen
import okhttp3.MultipartBody
import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation.compose.currentBackStackEntryAsState
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.compose.runtime.rememberCoroutineScope
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import za.co.skinnercafe.app.R

private object Routes {

    const val STARTUP = "startup"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val MY_ORDERS = "my_orders"
}

private fun Context.findActivity(): Activity? {
    return when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}

private suspend fun requestGoogleIdToken(context: Context): String {
    val activity = context.findActivity()
        ?: throw IllegalStateException(
            "Unable to access the current activity."
        )

    val credentialManager = CredentialManager.create(context)

    val googleOption = GetSignInWithGoogleOption.Builder(
        serverClientId = context.getString(
            R.string.default_web_client_id
        )
    ).build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleOption)
        .build()

    val result = credentialManager.getCredential(
        context = activity,
        request = request
    )

    val credential = result.credential

    if (
        credential !is CustomCredential ||
        credential.type !=
        GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {
        throw IllegalStateException(
            "Google returned an unsupported credential."
        )
    }

    return GoogleIdTokenCredential
        .createFrom(credential.data)
        .idToken
}

@Composable
fun SkinnerCafeNavigation(
    openMyOrdersRequest: Int = 0,
    homeContent: @Composable (
        CustomerDto?,
        () -> Unit,
        (MultipartBody.Part) -> Unit,
        () -> Unit,
        AuthUiState,
        () -> Unit,
        () -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val appPreferences = remember {
        AppPreferences(context.applicationContext)
    }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(appPreferences)
    )

    val authState by authViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()


    val startGoogleSignIn: () -> Unit = {
        coroutineScope.launch {
            try {
                val idToken = requestGoogleIdToken(context)

                authViewModel.loginWithGoogle(idToken) {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

            } catch (_: GetCredentialCancellationException) {
                // The user cancelled Google sign-in.
            } catch (exception: GoogleIdTokenParsingException) {
                Toast.makeText(
                    context,
                    "Unable to read your Google account. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: GetCredentialException) {
                Toast.makeText(
                    context,
                    exception.message ?: "Google sign-in failed.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: Exception) {
                Toast.makeText(
                    context,
                    exception.message
                        ?: "An unexpected sign-in error occurred.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val startGoogleAccountLink: () -> Unit = {
        coroutineScope.launch {
            try {
                val idToken = requestGoogleIdToken(context)

                authViewModel.linkGoogleAccount(idToken) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

            } catch (_: GetCredentialCancellationException) {
                // The user cancelled account linking.
            } catch (exception: GoogleIdTokenParsingException) {
                Toast.makeText(
                    context,
                    "Unable to read your Google account. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: GetCredentialException) {
                Toast.makeText(
                    context,
                    exception.message ?: "Google account linking failed.",
                    Toast.LENGTH_LONG
                ).show()
            } catch (exception: Exception) {
                Toast.makeText(
                    context,
                    exception.message
                        ?: "An unexpected account-linking error occurred.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    val startSetPassword: (String) -> Unit = { newPassword ->
        coroutineScope.launch {
            try {
                val idToken = requestGoogleIdToken(context)

                authViewModel.setPassword(idToken, newPassword) {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }

            } catch (_: GetCredentialCancellationException) {
                // The user cancelled password setup.

            } catch (exception: GoogleIdTokenParsingException) {
                Toast.makeText(
                    context,
                    "Unable to read your Google account. Please try again.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (exception: GetCredentialException) {
                Toast.makeText(
                    context,
                    exception.message ?: "Google verification failed.",
                    Toast.LENGTH_LONG
                ).show()

            } catch (exception: Exception) {
                Toast.makeText(
                    context,
                    exception.message ?: "Unable to set your password.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val lastHandledNotificationRequest = remember {
        mutableIntStateOf(0)
    }

    LaunchedEffect(
        openMyOrdersRequest,
        navBackStackEntry?.destination?.route
    ) {
        val currentRoute = navBackStackEntry?.destination?.route

        if (
            openMyOrdersRequest > lastHandledNotificationRequest.intValue &&
            currentRoute in listOf(
                Routes.HOME,
                Routes.SETTINGS,
                Routes.MY_ORDERS
            )
        ) {
            lastHandledNotificationRequest.intValue = openMyOrdersRequest

            if (currentRoute != Routes.MY_ORDERS) {
                navController.navigate(Routes.MY_ORDERS) {
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.STARTUP
    ) {
        composable(Routes.STARTUP) {
            LaunchedEffect(Unit) {
                authViewModel.checkSession(
                    onAuthenticated = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.STARTUP) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    },
                    onUnauthenticated = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.STARTUP) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (authState.isLoading || authState.errorMessage == null) {
                    CircularProgressIndicator()

                    Text(
                        text = "Checking your session...",
                        modifier = Modifier.padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                } else {
                    Text(
                        text = authState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Button(
                        onClick = {
                            authViewModel.checkSession(
                                onAuthenticated = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.STARTUP) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                },
                                onUnauthenticated = {
                                    navController.navigate(Routes.LOGIN) {
                                        popUpTo(Routes.STARTUP) {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }
                            )
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage,
                onLoginClick = { email, password ->
                    authViewModel.login(email, password) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onGoogleSignInClick = startGoogleSignIn,
                onSignUpClick = {
                    authViewModel.clearMessages()
                    navController.navigate(Routes.SIGN_UP)
                }
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onSignUpClick = { firstName, lastName, email, password ->
                    authViewModel.register(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = password
                    ) {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                onGoogleSignInClick = startGoogleSignIn,
                onLoginClick = {
                    authViewModel.clearMessages()
                    navController.popBackStack()
                },
                isLoading = authState.isLoading,
                errorMessage = authState.errorMessage
            )
        }

        composable(Routes.HOME) {
            homeContent(
                authState.customer,
                {
                    // Logout code stays here
                },
                authViewModel::uploadProfilePicture,
                authViewModel::removeProfilePicture,
                authState,
                {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                },
                {
                    navController.navigate(Routes.MY_ORDERS) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                customer = authState.customer,
                appPreferences = appPreferences,
                isLoading = authState.isLoading,
                successMessage = authState.successMessage,
                errorMessage = authState.errorMessage,
                hasPassword = authState.hasPassword,
                isGoogleLinked = authState.isGoogleLinked,

                onLinkGoogleAccount = {
                    authViewModel.clearMessages()
                    startGoogleAccountLink()
                },

                onSetPassword = { newPassword ->
                    authViewModel.clearMessages()
                    startSetPassword(newPassword)
                },

                onChangePassword = { currentPassword, newPassword ->
                    authViewModel.clearMessages()

                    authViewModel.changePassword(
                        currentPassword,
                        newPassword
                    ) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },

                onUnlinkGoogleAccount = { currentPassword ->
                    authViewModel.clearMessages()

                    authViewModel.unlinkGoogleAccount(currentPassword) {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },

                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },

                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.MY_ORDERS) {
            MyOrdersScreen(
                onBack = {
                    navController.popBackStack()
                },
                onSessionExpired = {
                    authViewModel.handleSessionExpired()

                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}