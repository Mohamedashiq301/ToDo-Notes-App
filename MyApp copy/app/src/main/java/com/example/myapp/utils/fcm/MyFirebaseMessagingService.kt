package com.example.myapp.utils.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.myapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService(){
    val TAG="FirebaseMsgService"
    override fun onMessageReceived(message: RemoteMessage?) {
        super.onMessageReceived(message)
        Log.d(TAG, message?.from.toString())
        //Log.d(TAG,message?.data.toString())
        Log.d(TAG,message?.notification?.body.toString())

        setupNotification(message?.notification?.body)

    }

    private fun setupNotification(body: String?) {
        val channelId="Todo Id"
        val ringtone=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder=NotificationCompat.Builder(this,channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Todo Notes App")
                .setContentText(body)
                .setSound(ringtone)

        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.O){
            val channel=NotificationChannel(channelId,"Todo Notes",NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(0,notificationBuilder.build())
    }

    override fun onNewToken(token: String?) {
        super.onNewToken(token)
    }
}