package za.co.skinnercafe.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import za.co.skinnercafe.app.data.api.CustomerDto
import za.co.skinnercafe.app.ui.auth.AuthUiState
import za.co.skinnercafe.app.ui.auth.AuthViewModel
import za.co.skinnercafe.app.ui.auth.LoginScreen
import za.co.skinnercafe.app.ui.auth.SignUpScreen
import za.co.skinnercafe.app.ui.settings.AppPreferences
import za.co.skinnercafe.app.ui.settings.SettingsScreen
import okhttp3.MultipartBody

private object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun SkinnerCafeNavigation(
    homeContent: @Composable (
        CustomerDto?,
        () -> Unit,
        (MultipartBody.Part) -> Unit,
        () -> Unit,
        AuthUiState,
        () -> Unit
    ) -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

    val context = LocalContext.current

    val appPreferences = remember {
        AppPreferences(context.applicationContext)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
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
                onGoogleSignInClick = {
                    // Google SSO will be implemented separately.
                },
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
                onGoogleSignInClick = {
                    // Google SSO will be implemented separately.
                },
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
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.HOME) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                authViewModel::uploadProfilePicture,
                authViewModel::removeProfilePicture,
                authState,
                {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                customer = authState.customer,
                appPreferences = appPreferences,
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
    }
}

