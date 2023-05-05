package com.firebase.chat.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.firebase.chat.R
import com.firebase.chat.ui.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobisharnam.domain.util.AppConstant
import java.util.Random

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("PrintToken","token -> ${token}")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.e("onMessageReceived","onMessageReceived -> ${message.messageId} ${message.senderId} ${message.notification}")

        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE  )
        val notifyID = 1
        val channelId = "ChatChannel"
        val name: CharSequence = "my_channel_01"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, name, importance)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val notification: Notification =
            NotificationCompat.Builder(this,channelId).setContentTitle(message.notification?.title)
                .setContentText(message.notification?.body)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setChannelId(channelId)
                .build()
        AppConstant.notificationID = Random().nextInt()
        mNotificationManager.notify(AppConstant.notificationID, notification)
    }
}