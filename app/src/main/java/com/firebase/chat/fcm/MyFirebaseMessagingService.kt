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
import androidx.core.app.RemoteInput
import com.firebase.chat.R
import com.firebase.chat.ui.activity.MainActivity
import com.firebase.chat.utils.ReplayNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobisharnam.domain.util.AppConstant

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        var CHAT_ID = ""
        const val KEY_TEXT_REPLY = "key_text_reply"
        val REQUEST_CODE_REPLAY = "NotificationReplay"
        val REQUEST_CODE_MARK_AS_READ = "NotificationMarkAsRead"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("PrintToken","token -> ${token}")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationId = message.data[AppConstant.NOTIFICATION_ID]
        val notificationChatId = message.data[AppConstant.CHAT_ID]
        AppConstant.notificationID = notificationId?.toInt() ?: 0
        Log.e("PrintRemoteMessage","onMessageReceived -> $notificationChatId ")
        Log.e("RemoteMessage","RemoteMessage ->")

        val intent = Intent(this, MainActivity::class.java)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent =  PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)
        val channelId = "ChatChannel"
        val name: CharSequence = "my_channel_01"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(channelId, name, importance)
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val replayIntent = Intent(this, ReplayNotification::class.java)
        replayIntent.action = "REPLY_ACTION"
        replayIntent.putExtra(REQUEST_CODE_REPLAY,REQUEST_CODE_REPLAY)
        replayIntent.putExtra(AppConstant.CHAT_ID,notificationChatId)

        val replayPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            replayIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .build()

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "replay", replayPendingIntent)
            .addRemoteInput(remoteInput)
            .build()

        val markAsReadIntent = Intent(this, ReplayNotification::class.java)
        markAsReadIntent.action = "MARK_AS_READ_ACTION"
        markAsReadIntent.putExtra(REQUEST_CODE_MARK_AS_READ,REQUEST_CODE_MARK_AS_READ)
        markAsReadIntent.putExtra(AppConstant.CHAT_ID,notificationChatId)
        markAsReadIntent.putExtra(AppConstant.NOTIFICATION_ID,notificationId)

        val markAsReadPendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            markAsReadIntent,
            PendingIntent.FLAG_MUTABLE
        )

        val markAsReadAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "Mark as Read", markAsReadPendingIntent)
            .build()

        val notification: Notification =
            NotificationCompat.Builder(this,channelId).setContentTitle(message.notification?.title)
                .setContentText(message.notification?.body)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(replyAction)
                .addAction(markAsReadAction)
                .setChannelId(channelId)
                .build()
        Log.e("notificationId","fcm -> $CHAT_ID $notificationChatId")
        if (CHAT_ID != notificationChatId) {
            mNotificationManager.notify(notificationId?.toInt() ?: 0, notification)
        }
    }
}