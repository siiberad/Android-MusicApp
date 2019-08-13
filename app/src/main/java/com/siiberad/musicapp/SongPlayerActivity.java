package com.siiberad.musicapp;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.FadingCircle;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.event.PlaybackEvent;
import com.siiberad.musicapp.event.PlaybackProgressEvent;
import com.siiberad.musicapp.event.PlaybackStatus;
import com.siiberad.musicapp.event.PlaybackStatusCheck;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SongPlayerActivity extends AppCompatActivity {

    private EventBus bus = EventBus.getDefault();

    ImageView img_prev_button_main, img_next_button_main, img_image_main;
    ImageButton img_button_play_pause_main;
    TextView txt_singer_main, txt_title_main, txt_total_duration, txt_current_position, txt_loading_text;

    String title, singer;
    int total, current_position;
    boolean isPlaying = true;

    ProgressBar progress_main;
    ImageLoader imageLoader;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_song_player);

        ImageLoader.getInstance().init(ImageLoaderConfiguration.createDefault(this));
        imageLoader = ImageLoader.getInstance();

        img_button_play_pause_main = (ImageButton) findViewById(R.id.play_pause_button_main);
        img_button_play_pause_main.setBackgroundResource(R.drawable.pause_oranges);
        img_button_play_pause_main.setVisibility(View.INVISIBLE);
        img_button_play_pause_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PAUSE));
                } else {
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_RESUME));
                }
            }
        });

        img_next_button_main = (ImageView) findViewById(R.id.next_button_main);
        img_next_button_main.setVisibility(View.INVISIBLE);
        img_next_button_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_image_main.setImageResource(0);
                EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_NEXT));
            }

        });

        img_prev_button_main = (ImageView) findViewById(R.id.prev_button_main);
        img_prev_button_main.setVisibility(View.INVISIBLE);
        img_prev_button_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                img_image_main.setImageResource(0);
                EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PREVIOUS));
            }

        });

        txt_title_main = (TextView) findViewById(R.id.title_main);
        txt_singer_main = (TextView) findViewById(R.id.singer_main);
        img_image_main = (ImageView) findViewById(R.id.image_main);
        txt_total_duration = (TextView) findViewById(R.id.total_duration);
        txt_loading_text = (TextView) findViewById(R.id.loading_text);
        txt_current_position = (TextView) findViewById(R.id.current_position);
        txt_current_position.setVisibility(View.INVISIBLE);

        seekBar = (SeekBar) findViewById(R.id.seeker_bar);
        seekBar.setVisibility(View.INVISIBLE);

        progress_main = (ProgressBar) findViewById(R.id.progress_main);
        Sprite pulse = new FadingCircle();
        progress_main.setIndeterminateDrawable(pulse);
        progress_main.setVisibility(View.VISIBLE);


    }


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
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                if (event != null) {
                    switch (event.getStatus()) {
                        case PlaybackEvent.STATE_PLAYED:
                            isPlaying = true;
                            total = event.getTotal_duration();
                            current_position = event.getCurrent_position();
                            progress_main.setVisibility(View.GONE);
                            txt_loading_text.setVisibility(View.GONE);
                            img_button_play_pause_main.setVisibility(View.VISIBLE);
                            img_next_button_main.setVisibility(View.VISIBLE);
                            img_prev_button_main.setVisibility(View.VISIBLE);
                            txt_current_position.setVisibility(View.VISIBLE);
                            txt_total_duration.setVisibility(View.VISIBLE);

                            seekBar.setVisibility(View.VISIBLE);
//                            seekBar.setMax(event.getTotal_duration());
                            seekBar.setProgress(current_position);
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                    txt_current_position.setVisibility(View.VISIBLE);
                                }

                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    if (isPlaying) {
                                        isPlaying = false;
                                        int seekProgress = seekBar.getProgress();
                                        EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_SEEKBAR,  seekProgress, event.getSongModel()));
                                    }
                                }
                            });

                            img_image_main.setVisibility(View.VISIBLE);
                            img_button_play_pause_main.setBackgroundResource(R.drawable.pause_oranges);
                            break;

                        case PlaybackEvent.STATE_BUFFERING:
                            title = event.getSongModel().getTitle();
                            singer = event.getSongModel().getSinger();
                            txt_title_main.setText(event.getSongModel().getTitle());
                            txt_singer_main.setText(event.getSongModel().getSinger());

                            txt_current_position.setVisibility(View.GONE);
                            txt_loading_text.setVisibility(View.VISIBLE);
                            txt_total_duration.setVisibility(View.GONE);
                            seekBar.setVisibility(View.GONE);
                            img_button_play_pause_main.setVisibility(View.GONE);
                            img_next_button_main.setVisibility(View.GONE);
                            img_prev_button_main.setVisibility(View.GONE);
                            img_image_main.setVisibility(View.GONE);

                            img_image_main.setImageResource(0);
                            imageLoader.displayImage(event.getSongModel().getImage(), img_image_main);

                            progress_main.setVisibility(View.VISIBLE);
                            break;

                        case PlaybackEvent.STATE_PAUSED:
                            isPlaying = false;
                            img_button_play_pause_main.setBackgroundResource(R.drawable.play_orange);

                            break;

                        case PlaybackEvent.STATE_RESUME:
                            isPlaying = true;
                            img_button_play_pause_main.setBackgroundResource(R.drawable.pause_oranges);
                            break;

                            case PlaybackEvent.STATE_SEEKBAR:
                            isPlaying = true;
                            img_button_play_pause_main.setBackgroundResource(R.drawable.pause_oranges);
                            break;
                    }
                }
            }
        });

    }

    public void getSeekBarStatus(){

        new Thread(new Runnable() {

            @Override
            public void run() {
                // mp is your MediaPlayer
                // progress is your ProgressBar

                int currentPosition = 0;
                seekBar.setMax(total);
                while (currentPosition < total) {
                    try {
                        Thread.sleep(1000);
                        currentPosition = current_position;
                    } catch (InterruptedException e) {
                        return;
                    }
                    seekBar.setProgress(currentPosition);
                }
            }
        }).start();





        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress=0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, int ProgressValue, boolean fromUser) {
//                if (fromUser) {
//                    mp.seekTo(ProgressValue);//if user drags the seekbar, it gets the position and updates in textView.
//                }
                final long mMinutes=(ProgressValue/1000)/60;//converting into minutes
                final int mSeconds=((ProgressValue/1000)%60);//converting into seconds
                txt_current_position.setText(mMinutes+":"+mSeconds);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }



    @Subscribe
    public void OnEvent(final PlaybackStatus event){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(event!=null){
                    switch (event.getStatus()){
                        case PlaybackStatus.STATUS_PLAY:
                            isPlaying = true;
                            txt_title_main.setText(event.getSongModel().getTitle());
                            txt_singer_main.setText(event.getSongModel().getSinger());
                            txt_current_position.setText(Integer.toString(event.getSongModel().getDuration()));
                            seekBar.setProgress(event.getSongModel().getCurrentPosition());
                            seekBar.setMax(event.getSongModel().getDuration());
                            img_button_play_pause_main.setBackgroundResource(R.drawable.pause_oranges);

                            break;

                        case PlaybackStatus.STATUS_PAUSE:
                            isPlaying = false;
                            txt_title_main.setText(event.getSongModel().getTitle());
                            txt_singer_main.setText(event.getSongModel().getSinger());
                            txt_current_position.setText(Integer.toString(event.getSongModel().getDuration()));
                            img_button_play_pause_main.setBackgroundResource(R.drawable.play_orange);
                            break;
                    }
                }
            }
        });

    }


    @Subscribe
    public void OnEvent(final PlaybackProgressEvent event){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                seekBar.setProgress(event.getCurrentPosition());
                seekBar.setMax(event.getTotalDuration());

                txt_current_position.setText(milliSecondsToTimer(event.getCurrentPosition()));
                txt_total_duration.setText(milliSecondsToTimer(event.getTotalDuration()));
            }
        });
    }

    public String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        }   else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

}
