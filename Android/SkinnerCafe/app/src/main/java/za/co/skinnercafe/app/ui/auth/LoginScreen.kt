package za.co.skinnercafe.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextAlign

private val CafeGreen = Color(0xFF315744)
private val CafeDarkGreen = Color(0xFF183D2C)
private val CafeCream = Color(0xFFF7F4EC)
private val CafeMuted = Color(0xFF77796F)
private val CafeGold = Color(0xFFD8BB83)

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CafeCream)
            .padding(horizontal = 28.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SKINNER CAFE",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = CafeDarkGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your favourite coffee, your way.",
            style = MaterialTheme.typography.bodyMedium,
            color = CafeMuted
        )

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = CafeDarkGreen
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to your account",
            color = CafeMuted
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email address") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { /* Add password recovery later */ },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(
                text = "Forgot password?",
                color = CafeGreen
            )
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank() && !isLoading) {
                    onLoginClick(email.trim(), password)
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CafeGreen
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedButton(
            onClick = onGoogleSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Continue with Google",
                color = CafeDarkGreen
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onSignUpClick) {
            Text(
                text = "Don't have an account? Sign up",
                color = CafeGreen
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Freshly made. Always welcoming.",
            fontSize = 12.sp,
            color = CafeMuted
        )
    }
}
