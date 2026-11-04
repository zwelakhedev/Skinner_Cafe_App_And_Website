package za.co.skinnercafe.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import za.co.skinnercafe.app.ui.auth.AuthViewModel
import za.co.skinnercafe.app.ui.auth.LoginScreen
import za.co.skinnercafe.app.ui.auth.SignUpScreen
import za.co.skinnercafe.app.data.api.CustomerDto
import okhttp3.MultipartBody
import za.co.skinnercafe.app.ui.auth.AuthUiState

private object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
}

@Composable
fun SkinnerCafeNavigation(
    homeContent: @Composable (
        CustomerDto?,
        () -> Unit,
        (MultipartBody.Part) -> Unit,
        () -> Unit,
        AuthUiState
    ) -> Unit
) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.uiState.collectAsState()

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
                authState
            )
        }
    }
}

