package com.siiberad.musicapp.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.siiberad.musicapp.MainActivity;
import com.siiberad.musicapp.R;
import com.siiberad.musicapp.SongPlayerActivity;
import com.siiberad.musicapp.event.PlaybackCommand;

import org.greenrobot.eventbus.EventBus;

public class NotificationActionReceiverPlay extends BroadcastReceiver {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {

        Boolean play = intent.getBooleanExtra("playaction", true);

        if (play.equals(true)) {

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Intent intentPause = new Intent(context, NotificationActionReceiverPause.class);
            PendingIntent pendingIntentPause = PendingIntent.getBroadcast(context, 0, intentPause, 0);

            Intent intentNext = new Intent(context, NotificationActionReceiverNext.class);
            PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, 0);

            Intent intentPrevious = new Intent(context, NotificationActionReceiverPrevious.class);
            PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, 0);

            Notification mBuilder = new NotificationCompat.Builder(context)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.audio)
                    .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                    .setContentTitle("asdasd")
                    .setContentText("asdasdasdad")
                    .setWhen(System.currentTimeMillis())
                    .addAction(R.drawable.ic_skip_previous_black_24dp, "", pendingIntentPrevious)
                    .addAction(R.drawable.pause_oranges, "", pendingIntentPause)
                    .addAction(R.drawable.ic_skip_next_black_24dp, "", pendingIntentNext)
                    .build();


            NotificationManager mNotificationManager =

                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(001, mBuilder.clone());

            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_RESUME));

        }
    }
}
