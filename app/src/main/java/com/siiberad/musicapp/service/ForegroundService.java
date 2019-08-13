package com.siiberad.musicapp.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.siiberad.musicapp.MainActivity;
import com.siiberad.musicapp.R;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.event.PlaybackEvent;
import com.siiberad.musicapp.event.PlaybackProgressEvent;
import com.siiberad.musicapp.event.PlaybackStatus;
import com.siiberad.musicapp.event.PlaybackStatusCheck;
import com.siiberad.musicapp.model.SongModel;
import com.siiberad.musicapp.receiver.NotificationActionReceiverNext;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPause;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPlay;
import com.siiberad.musicapp.receiver.NotificationActionReceiverPrevious;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ForegroundService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 111;

    String singer, title;
    int index, total_duration, currentPosition;

    MediaPlayer mPlayer;
    EventBus bus = EventBus.getDefault();
    Notification notification;

    SongModel currentlyPlayedSong,nextPlayedSong;
    List<SongModel> songModels;
    private PendingIntent pendingIntent;
    private PendingIntent pendingIntentPause;
    private PendingIntent pendingIntentPlay;
    private PendingIntent pendingIntentNext;
    private PendingIntent pendingIntentPrevious;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (!bus.isRegistered(this)) {
            bus.register(this);
        }

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                notif(PlaybackCommand.COMMAND_PLAY);
                mPlayer.start();
                total_duration = mPlayer.getDuration();
                currentPosition = mPlayer.getCurrentPosition();



                EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_PLAYED, total_duration, currentPosition, currentlyPlayedSong));

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        while (mPlayer.isPlaying() && mPlayer.getCurrentPosition()<mPlayer.getDuration()){
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            EventBus.getDefault().post(new PlaybackProgressEvent(mPlayer.getCurrentPosition(),mPlayer.getDuration()));
                        }
                        return null;
                    }
                }.execute();

            }
        });

        Intent notificationIntent = new Intent(this, MainActivity.class);
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Intent intentPause = new Intent(this, NotificationActionReceiverPause.class);
        pendingIntentPause = PendingIntent.getBroadcast(this, UUID.randomUUID().hashCode(), intentPause, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPlay = new Intent(this, NotificationActionReceiverPlay.class);
        pendingIntentPlay = PendingIntent.getBroadcast(this, UUID.randomUUID().hashCode(), intentPlay, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentNext = new Intent(this, NotificationActionReceiverNext.class);
        pendingIntentNext = PendingIntent.getBroadcast(this, 0, intentNext, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intentPrevious = new Intent(this, NotificationActionReceiverPrevious.class);
        pendingIntentPrevious = PendingIntent.getBroadcast(this, 0, intentPrevious, PendingIntent.FLAG_UPDATE_CURRENT);

        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) { if (songModels != null) {

//                    mPlayer.stop();
                    mPlayer.reset();
//                    mPlayer.release();

                    if ((index + 1) == songModels.size()) {
                        index = 0;
                        nextPlayedSong = songModels.get(index);
                    } else {
                        nextPlayedSong = songModels.get(index + 1);
                        index++;
                    }
                    try {
                        mPlayer.setDataSource(nextPlayedSong.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    title = nextPlayedSong.getTitle();
                    singer = nextPlayedSong.getSinger();

                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepareAsync();

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, nextPlayedSong));

                }
            }

        });


        createNotificationChannel();

        return START_STICKY;
    }

    @Subscribe
    public void OnEvent(PlaybackCommand event) {
        if (event != null) {
            switch (event.getCommand()) {
                case PlaybackCommand.COMMAND_PLAY:
                    if(mPlayer.isPlaying()) {
                        mPlayer.stop();
                        mPlayer.reset();
                    }
                    index = event.getIndex();
                    songModels = event.getSongModels();
                    currentlyPlayedSong = songModels.get(index);
                    try {
                        mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));

                    title = currentlyPlayedSong.getTitle();
                    singer = currentlyPlayedSong.getSinger();

                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepareAsync();
                    break;

                case PlaybackCommand.COMMAND_PAUSE:
                    mPlayer.seekTo(event.getRemain_duration());
                    mPlayer.pause();
                    notif(PlaybackCommand.COMMAND_PAUSE);
                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_PAUSED, total_duration , currentPosition, currentlyPlayedSong));
                    break;

                case PlaybackCommand.COMMAND_RESUME:
                    mPlayer.start();
                    notif(PlaybackCommand.COMMAND_PLAY);
                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_RESUME, total_duration , currentPosition, currentlyPlayedSong));
                    break;

                case PlaybackCommand.COMMAND_NEXT:
                    mPlayer.stop();
                    mPlayer.reset();

                    if ((index + 1) == songModels.size()) {
                        index = 0;
                        currentlyPlayedSong = songModels.get(index);
                    } else {
                        currentlyPlayedSong = songModels.get(index + 1);
                        index++;
                    }

                    try {
                        mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));

                    title = currentlyPlayedSong.getTitle();
                    singer = currentlyPlayedSong.getSinger();

                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepareAsync();


                    break;

                case PlaybackCommand.COMMAND_PREVIOUS:
                    mPlayer.stop();
                    mPlayer.reset();

                    if ((index - 1) == -1) {
                        index = 0;
                        currentlyPlayedSong = songModels.get(index);
                    } else {
                        currentlyPlayedSong = songModels.get(index - 1);
                        index--;
                    }

                    try {
                        mPlayer.setDataSource(currentlyPlayedSong.getUrl());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_BUFFERING, currentlyPlayedSong));

                    title = currentlyPlayedSong.getTitle();
                    singer = currentlyPlayedSong.getSinger();

                    mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mPlayer.prepareAsync();

                    break;

                case PlaybackCommand.COMMAND_SEEKBAR:
                    mPlayer.seekTo(event.getRemain_duration());
                    currentPosition =mPlayer.getCurrentPosition();
//                    mPlayer.start();
                    notif(PlaybackCommand.COMMAND_PLAY);
                    EventBus.getDefault().post(new PlaybackEvent(PlaybackEvent.STATE_SEEKBAR, currentPosition, currentlyPlayedSong));

                    currentlyPlayedSong.setCurrentPosition(mPlayer.getCurrentPosition());
                    currentlyPlayedSong.setDuration(mPlayer.getDuration());

                    EventBus.getDefault().post(new PlaybackStatus(PlaybackStatus.STATUS_PLAY, currentlyPlayedSong));
                    break;
            }
        }
    }

//    public void run() {
//        int current;
//        while (mPlayer.isPlaying() && currentPosition < total_duration) {
//            try {
//                Thread.sleep(1000);
//                current = currentPosition;
//            } catch (InterruptedException e) {
//                return;
//            } catch (Exception e) {
//                return;
//            }
//        }
//    }

    @Subscribe
    public void OnEvent(PlaybackStatusCheck event) {
        if (event != null) {
            if (mPlayer.isPlaying()) {
                currentlyPlayedSong.setCurrentPosition(mPlayer.getCurrentPosition());
                currentlyPlayedSong.setDuration(mPlayer.getDuration());

                EventBus.getDefault().post(new PlaybackStatus(PlaybackStatus.STATUS_PLAY, currentlyPlayedSong));
            } else {
                EventBus.getDefault().post(new PlaybackStatus(PlaybackStatus.STATUS_PAUSE, currentlyPlayedSong));
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

    public void notif(int mode) {
        switch (mode) {
            case PlaybackCommand.COMMAND_PLAY:
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
                break;

            case PlaybackCommand.COMMAND_PAUSE:
                notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.audio)
                        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                        .setContentTitle(title)
                        .setContentText(singer)
                        .setWhen(System.currentTimeMillis())
                        .setContentIntent(pendingIntent)
                        .addAction(R.drawable.ic_skip_previous_black_24dp, getString(R.string.previous), pendingIntentPrevious)
                        .addAction(R.drawable.play_orange, getString(R.string.pause), pendingIntentPlay)
                        .addAction(R.drawable.ic_skip_next_black_24dp, getString(R.string.next), pendingIntentNext)
                        .build();
                break;


        }


        startForeground(NOTIFICATION_ID, notification);

    }

    public void playSong(){

    }

}