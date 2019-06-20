package com.siiberad.musicapp.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.siiberad.musicapp.MainActivity;
import com.siiberad.musicapp.R;
import com.siiberad.musicapp.SongPlayerActivity;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.service.ForegroundService;

import org.greenrobot.eventbus.EventBus;

import static com.siiberad.musicapp.service.ForegroundService.CHANNEL_ID;

public class NotificationActionReceiverPause extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        Boolean title = intent.getBooleanExtra("title", true);

        if(title) {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            Intent notificationIntent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Intent intentPlay = new Intent(context, NotificationActionReceiverPlay.class);
            PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(context, 0, intentPlay, 0);

            Intent intentNext = new Intent(context, NotificationActionReceiverNext.class);
            PendingIntent pendingIntentNext = PendingIntent.getBroadcast(context, 0, intentNext, 0);

            Intent intentPrevious = new Intent(context, NotificationActionReceiverPrevious.class);
            PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(context, 0, intentPrevious, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.audio)
                        .setContentTitle("123123123")
                        .setContentText("asdasdasdad")
                        .setWhen(System.currentTimeMillis())
                        .addAction(R.drawable.ic_skip_previous_black_24dp, "", pendingIntentPrevious)
                        .addAction(R.drawable.play_orange, "", pendingIntentPlay)
                        .setOnlyAlertOnce(true)
                        .addAction(R.drawable.ic_skip_next_black_24dp, "", pendingIntentNext);


            notificationManager.notify(1, builder.build());

//            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//            mNotificationManager.notify(001, mBuilder.clone());


            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PAUSE));

        }

    }

//    @Override
//    public  void onReceive(Context context, Intent intent){
//
//        if (intent.getAction().equals(ForegroundService.NOTIFY_PLAY)){
//            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PLAY));
//        }else if (intent.getAction().equals(ForegroundService.NOTIFY_PAUSE)){
//            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PAUSE));
//        }else if (intent.getAction().equals(ForegroundService.NOTIFY_NEXT)){
//            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_NEXT));
//        }else if (intent.getAction().equals(ForegroundService.NOTIFY_PREVIOUS)){
//            EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PREVIOUS));
//        }
//    }

}
