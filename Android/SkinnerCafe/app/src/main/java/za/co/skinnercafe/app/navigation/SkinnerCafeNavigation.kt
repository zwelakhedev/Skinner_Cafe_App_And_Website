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

private object Routes {

    const val STARTUP = "startup"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val MY_ORDERS = "my_orders"
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

