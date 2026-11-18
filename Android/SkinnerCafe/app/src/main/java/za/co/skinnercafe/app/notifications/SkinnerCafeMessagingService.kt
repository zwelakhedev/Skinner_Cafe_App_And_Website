package za.co.skinnercafe.app.notifications

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import za.co.skinnercafe.app.R
import za.co.skinnercafe.app.ui.settings.AppPreferences
import za.co.skinnercafe.app.data.api.AuthRepository
import za.co.skinnercafe.app.data.api.MenuApiClient
import android.app.PendingIntent
import android.content.Intent
import za.co.skinnercafe.app.MainActivity

class SkinnerCafeMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d("SkinnerCafeFCM", "FCM token refreshed.")

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                AppPreferences(applicationContext)
                    .saveFcmDeviceToken(token)

                Log.d("SkinnerCafeFCM", "FCM token saved locally.")

                // Initialize the API client for background use.
                MenuApiClient.initialize(applicationContext)

                val repository = AuthRepository()

                // Only register the token if a customer session exists.
                repository.getCurrentCustomer()

                repository.registerDeviceToken(token)

                Log.d(
                    "SkinnerCafeFCM",
                    "Refreshed FCM token registered with the API."
                )

            } catch (exception: Exception) {
                Log.e(
                    "SkinnerCafeFCM",
                    "FCM token saved locally, but server synchronization failed or no customer session exists.",
                    exception
                )
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val preferences = AppPreferences(applicationContext)

                if (!preferences.notificationsEnabled.first()) {
                    Log.d(
                        "SkinnerCafeFCM",
                        "Notification skipped because notifications are disabled."
                    )
                    return@launch
                }

                val title = message.data["title"]
                    ?: message.notification?.title
                    ?: "Skinner Cafe"

                val body = message.data["body"]
                    ?: message.notification?.body
                    ?: "Your order has an update."

                showOrderNotification(title, body)

            } catch (exception: Exception) {
                Log.e(
                    "SkinnerCafeFCM",
                    "Failed to process incoming notification.",
                    exception
                )
            }
        }
    }

    private fun showOrderNotification(
        title: String,
        body: String
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(
                "SkinnerCafeFCM",
                "Notification permission has not been granted."
            )
            return
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP

            putExtra(
                NotificationDestinations.EXTRA_OPEN_MY_ORDERS,
                true
            )
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            this,
            SkinnerCafeNotificationChannel.CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(body)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(this).notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}

