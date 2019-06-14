package com.siiberad.musicapp.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.siiberad.musicapp.MainActivity;
import com.siiberad.musicapp.R;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.event.PlaybackEvent;
import com.siiberad.musicapp.event.PlaybackStatus;
import com.siiberad.musicapp.event.PlaybackStatusCheck;
import com.siiberad.musicapp.model.SongModel;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPause;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPlay;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;

public class ForegroundService extends Service {


    MediaPlayer mPlayer;
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    EventBus bus;
    SongModel currentlyPlayedSong;
    private Notification notification;

    String singer, title;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        String singer = intent.getStringExtra("inputExtra");

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
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        createNotificationChannel();


//        stopForeground(false);

//        stopSelf();
        return START_NOT_STICKY;
    }

    @Subscribe
    public void OnEvent(PlaybackCommand event){
        if(event!=null){
            switch (event.getCommand()){
                case PlaybackCommand.COMMAND_PLAY:
                    try {
                        mPlayer.stop();
                        mPlayer.reset();
                        currentlyPlayedSong = event.getSongModel();
                        mPlayer.setDataSource(event.getSongModel().getUrl());
                        title = event.getSongModel().getTitle();
                        singer = event.getSongModel().getSinger();
                        mPlayer.prepareAsync(); // might take long! (for buffering, etc)
                        EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, event.getSongModel()));
                        //TODO:
                        //cari cara update notification text sesuai model

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;

                case PlaybackCommand.COMMAND_PAUSE:
                    mPlayer.pause();
//                    lengthSong = mPlayer.getCurrentPosition();
                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_PAUSED));
                    break;

                case PlaybackCommand.COMMAND_RESUME:
//                    mPlayer.seekTo(lengthSong);
                    mPlayer.start();
                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_RESUME));
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
                EventBus.getDefault().post(new PlaybackStatus(PlaybackStatus.STATUS_PAUSE));
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    public void notif(){

        Intent intentPlay = new Intent(this, NotificationActionReceiverPlay.class);
        intentPlay.putExtra("playaction","play");

        Intent intentPause = new Intent(this, NotificationActionReceiverPause.class);
        intentPause.putExtra("pauseaction","pause");

        PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 0, intentPlay, 0);
        PendingIntent pendingIntentPause = PendingIntent.getBroadcast(this, 0, intentPause, 0);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.audio)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentTitle(title)
                .setContentText(singer)
                .setTicker("Notification!")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.play_orange, getString(R.string.play), pendingIntentPlay)
                .addAction(R.drawable.pause_oranges, getString(R.string.pause), pendingIntentPause)
                .build();

        startForeground(1, notification);
    }

}