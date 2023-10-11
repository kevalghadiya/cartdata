package com.egarxpress.utils_module

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bistrocart.R
import com.bistrocart.base.BaseActivity
import com.bistrocart.main_module.activity.HomeScreenActivity
import com.bistrocart.main_module.activity.MyOrderDetailsActivity
import com.bistrocart.utils.AppPref
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseService : FirebaseMessagingService() {

    private var appPref: AppPref? = null

    override fun onCreate() {
        super.onCreate()
        appPref = AppPref.getInstance(this)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.e(TAG, "onMessageReceived: " + remoteMessage?.getData())
        Log.e(TAG, "onMessageReceived: " + remoteMessage)

        if (remoteMessage?.data?.size!! > 0) {
            Log.e(TAG, "Message data payload: " + remoteMessage.data)
            if (remoteMessage.data != null) {

                var jsonObject: JSONObject? = null
                try {
                  //  jsonObject = JSONObject(remoteMessage.data.toString())
                    Log.e(TAG, "Message data payload1: "+ jsonObject)
                    jsonObject = JSONObject(remoteMessage.data["message"])
                    Log.e(TAG, "Message data payload2: "+ jsonObject)
                   setNotification(jsonObject)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }

    }

    private fun setNotification(jsonObject: JSONObject) {
            var intent: Intent? = null

           /* if (!appPref?.isLogin()!! == true) {
                intent = Intent(this, HomeScreenActivity::class.java)
            } else {*/
                val bundle = Bundle()
                bundle.putString("order_id",jsonObject.optString("order_id"))
                bundle.putBoolean("notification",true)
                intent = Intent(this, MyOrderDetailsActivity::class.java)
                intent.putExtras(bundle)
         //   }

            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
            } else {
                TODO("VERSION.SDK_INT < M")
            }
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(jsonObject.optString("title"))
                .setContentText(jsonObject.optString("message"))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Create the notification channel for Android Oreo and above.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    this.getString(R.string.app_name) + "_01",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(notificationId, notificationBuilder.build())
      //  }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        Log.e(TAG, "onNewToken(): $s")
        Log.e(TAG, "onNewTokenFMC--->>: $s")
        appPref?.set(AppPref.FMCTOKEN, s)
    }

    companion object {
        private val TAG = MyFirebaseService::class.java.simpleName
        private const val CHANNEL_ID ="_01"
        private const val notificationId = 12345
    }
}
