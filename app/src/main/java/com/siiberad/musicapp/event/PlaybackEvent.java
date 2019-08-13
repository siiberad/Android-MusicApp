package com.siiberad.musicapp.event;

import com.siiberad.musicapp.model.SongModel;

public class PlaybackEvent {


    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYED = 1;
    public static final int STATE_BUFFERING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_RESUME = 4;
    public static final int STATE_SEEKBAR = 5;

    private int status;
    private int total_duration;
    private int current_position;
    private SongModel songModel;

    public PlaybackEvent(int status) {
        this.status = status;
    }

    public PlaybackEvent(int status, SongModel songModel) {
        this.status = status;
        this.songModel = songModel;
    }

    public PlaybackEvent(int status, int remain_duration, SongModel songModel) {
        this.status = status;
        this.current_position = remain_duration;
        this.songModel = songModel;
    }

    public PlaybackEvent(int status, int total_duration, int current_position, SongModel songModel) {
        this.status = status;
        this.total_duration = total_duration;
        this.current_position = current_position;
        this.songModel = songModel;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public SongModel getSongModel() {
        return songModel;
    }

    public void setSongModel(SongModel songModel) {
        this.songModel = songModel;
    }

    public int getTotal_duration() {
        return total_duration;
    }

    public void setTotal_duration(int total_duration) {
        this.total_duration = total_duration;
    }

    public int getCurrent_position() {
        return current_position;
    }

    public void setCurrent_position(int current_position) {
        this.current_position = current_position;
    }
}
