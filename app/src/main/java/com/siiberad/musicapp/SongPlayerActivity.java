package com.siiberad.musicapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.ChasingDots;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.github.ybq.android.spinkit.style.FoldingCube;
import com.github.ybq.android.spinkit.style.Pulse;
import com.github.ybq.android.spinkit.style.ThreeBounce;
import com.github.ybq.android.spinkit.style.Wave;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.event.PlaybackEvent;
import com.siiberad.musicapp.event.PlaybackStatus;
import com.siiberad.musicapp.event.PlaybackStatusCheck;
import com.siiberad.musicapp.service.ForegroundService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SongPlayerActivity extends AppCompatActivity {

    private EventBus bus;

    ImageView prev_button_main, next_button_main;
    ImageButton play_pause_button_main;
    TextView singer_main, title_main, remain_duration;
    ProgressBar progress_main;

    boolean isPlaying = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_song_player);



        play_pause_button_main = (ImageButton) findViewById(R.id.play_pause_button_main);
        play_pause_button_main.setBackgroundResource(R.drawable.pause_oranges);
        play_pause_button_main.setVisibility(View.INVISIBLE);
        play_pause_button_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PAUSE));
                } else {
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_RESUME));
                }
            }

        });

        next_button_main = (ImageView) findViewById(R.id.next_button_main);
        next_button_main.setVisibility(View.INVISIBLE);
        next_button_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SongPlayerActivity.this, "Next Song", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_NEXT));
            }

        });

        prev_button_main = (ImageView) findViewById(R.id.prev_button_main);
        prev_button_main.setVisibility(View.INVISIBLE);
        prev_button_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SongPlayerActivity.this, "Previous Song", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PREVIOUS));
            }

        });

        title_main = (TextView) findViewById(R.id.title_main);
        singer_main = (TextView) findViewById(R.id.singer_main);
        remain_duration = (TextView) findViewById(R.id.remain_duration);

        progress_main = (ProgressBar) findViewById(R.id.progress_main);
        Sprite pulse = new FadingCircle();
        progress_main.setIndeterminateDrawable(pulse);
        progress_main.setVisibility(View.VISIBLE);

        bus = EventBus.getDefault();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (!bus.isRegistered(this)) {
//            bus.register(this);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!bus.isRegistered(this)) {
            bus.register(this);
            EventBus.getDefault().post(new PlaybackStatusCheck());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Subscribe
    public void OnEvent(final PlaybackEvent event) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (event != null) {
                    switch (event.getStatus()) {
                        case PlaybackEvent.STATE_PLAYED:
                            isPlaying = true;
                            play_pause_button_main.setVisibility(View.VISIBLE);
                            next_button_main.setVisibility(View.VISIBLE);
                            prev_button_main.setVisibility(View.VISIBLE);
                            progress_main.setVisibility(View.INVISIBLE);
                            play_pause_button_main.setBackgroundResource(R.drawable.pause_oranges);
                            break;

                        case PlaybackEvent.STATE_BUFFERING:
                            title_main.setText(event.getSongModel().getTitle());
                            singer_main.setText(event.getSongModel().getSinger());
                            remain_duration.setText(event.getSongModel().getDuration());
                            play_pause_button_main.setVisibility(View.INVISIBLE);
                            next_button_main.setVisibility(View.INVISIBLE);
                            prev_button_main.setVisibility(View.INVISIBLE);
                            progress_main.setVisibility(View.VISIBLE);
                            break;

                        case PlaybackEvent.STATE_PAUSED:
                            isPlaying = false;
                            play_pause_button_main.setBackgroundResource(R.drawable.play_orange);
                            break;

                        case PlaybackEvent.STATE_RESUME:
                            isPlaying = true;
                            play_pause_button_main.setBackgroundResource(R.drawable.pause_oranges);
                            break;
                    }
                }
            }
        });

    }
//    @Subscribe
//    public void OnEvent(final PlaybackStatus event){
//
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if(event!=null){
//                    switch (event.getStatus()){
//                        case PlaybackStatus.STATUS_PLAY:
//                            isPlaying = true;
//                            title_main.setText(event.getSongModel().getTitle());
//                            singer_main.setText(event.getSongModel().getSinger());
//                            remain_duration.setText(event.getSongModel().getDuration());
//                            play_pause_button_main.setBackgroundResource(R.drawable.pause_oranges);
//                            break;
//
//                        case PlaybackStatus.STATUS_PAUSE:
//                            isPlaying = false;
//                            title_main.setText(event.getSongModel().getTitle());
//                            singer_main.setText(event.getSongModel().getSinger());
//                            remain_duration.setText(event.getSongModel().getDuration());
//                            play_pause_button_main.setBackgroundResource(R.drawable.play_orange);
//                            break;
//                    }
//                }
//            }
//        });
//
//    }

}
