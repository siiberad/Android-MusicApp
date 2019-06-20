package com.siiberad.musicapp.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.siiberad.musicapp.MainActivity;
import com.siiberad.musicapp.R;
import com.siiberad.musicapp.SongPlayerActivity;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.event.PlaybackEvent;
import com.siiberad.musicapp.event.PlaybackStatus;
import com.siiberad.musicapp.event.PlaybackStatusCheck;
import com.siiberad.musicapp.event.Title;
import com.siiberad.musicapp.model.SongModel;
import com.siiberad.musicapp.receiver.NotificationActionReceiverNext;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPause;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPlay;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPrevious;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ForegroundService extends Service{

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    String singer, title;
    int index;

    MediaPlayer mPlayer;
    EventBus bus;
    Notification notification;

    SongModel currentlyPlayedSong;
    List<SongModel> songModels;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        bus = EventBus.getDefault();
        if(!bus.isRegistered(this)) {
            bus.register(this);
        }

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                notif();
                mPlayer.start();
                EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_PLAYED, currentlyPlayedSong));
            }
        });

                //        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                //            @Override
                //            public void onCompletion(MediaPlayer mp) {
                //                if (songModels != null) {
                //                    mPlayer.stop();
                //                    mPlayer.reset();
                //
                //                    if ((index + 1) == songModels.size()) {
                //                        index = 0;
                //                        currentlyPlayedSong = songModels.get(index);
                //                    } else {
                //                        currentlyPlayedSong = songModels.get(index + 1);
                //                        index++;
                //                    }
                //
                //                    try {
                //                        mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                //                    } catch (IOException e) {
                //                        e.printStackTrace();
                //                    }
                //                    title = currentlyPlayedSong.getTitle();
                //                    singer = currentlyPlayedSong.getSinger();
                //                    mPlayer.prepareAsync();
                //                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));
                //                }
                //            }
                //
                //        });

        createNotificationChannel();

        return START_STICKY;
    }

    @Subscribe
    public void OnEvent(PlaybackCommand event){
        if(event!=null){
            switch (event.getCommand()){
                case PlaybackCommand.COMMAND_PLAY:
                        mPlayer.stop();
                        mPlayer.reset();
                        index = event.getIndex();
                        songModels = event.getSongModels();
                        currentlyPlayedSong = songModels.get(index);
                        try {
                            mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        title = currentlyPlayedSong.getTitle();
                        singer = currentlyPlayedSong.getSinger();
                        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mPlayer.prepareAsync();
                        EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));

                    break;

                case PlaybackCommand.COMMAND_PAUSE:
                    mPlayer.pause();

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_PAUSED));
                    break;

                case PlaybackCommand.COMMAND_RESUME:

                    mPlayer.start();

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_RESUME));
                    break;

                case PlaybackCommand.COMMAND_NEXT:
                    mPlayer.stop();
                    mPlayer.reset();

                    if((index + 1) == songModels.size()){
                        index = 0;
                        currentlyPlayedSong = songModels.get(index);
                    }else{
                        currentlyPlayedSong = songModels.get(index + 1);
                        index++;
                    }

                    try {
                        mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    title = currentlyPlayedSong.getTitle();
                    singer = currentlyPlayedSong.getSinger();
                    mPlayer.prepareAsync();

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));

                    break;

                case PlaybackCommand.COMMAND_PREVIOUS:
                    mPlayer.stop();
                    mPlayer.reset();

                    if((index - 1) == -1){
                        index = 0;
                        currentlyPlayedSong = songModels.get(index);
                    }else{
                        currentlyPlayedSong = songModels.get(index - 1);
                        index--;
                    }

                    try {
                        mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    title = currentlyPlayedSong.getTitle();
                    singer = currentlyPlayedSong.getSinger();
                    mPlayer.prepareAsync();
                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));

                    break;
            }
        }
    }

    @Subscribe
    public void OnEvent(PlaybackStatusCheck event){
        if(event!=null){
            if(mPlayer.isPlaying()){
                EventBus.getDefault().post(new PlaybackStatus(PlaybackStatus.STATUS_PLAY,currentlyPlayedSong));
            }else{
                EventBus.getDefault().post(new PlaybackStatus(PlaybackStatus.STATUS_PAUSE,currentlyPlayedSong));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, "Foreground Service Channel", NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public void notif(){

//        RemoteViews expandedView = new RemoteViews(context.getPackageName(), R.layout.big_notification);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent intentPause = new Intent(this, NotificationActionReceiverPause.class);
        intentPause.putExtra("title",true);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 0, intentPause, 0);

        Intent intentNext = new Intent(this, NotificationActionReceiverNext.class);
        intentNext.putExtra("nextaction",true);
        PendingIntent pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, 0);

        Intent intentPrevious = new Intent(this, NotificationActionReceiverPrevious.class);
        intentPrevious.putExtra("previousaction",true);
        PendingIntent pendingIntentPrevious = PendingIntent.getBroadcast(this, 0, intentPrevious, 0);


        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.audio)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(title)
                .setContentText(singer)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_skip_previous_black_24dp, getString(R.string.previous), pendingIntentPrevious)
                .addAction(R.drawable.pause_oranges, getString(R.string.pause), pendingIntentPause)
                .addAction(R.drawable.ic_skip_next_black_24dp, getString(R.string.next), pendingIntentNext)
                .build();

        startForeground(1, notification);

    }

}