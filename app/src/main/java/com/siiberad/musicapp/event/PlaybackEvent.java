package com.siiberad.musicapp.event;

import com.siiberad.musicapp.model.SongModel;

public class PlaybackEvent {


    public static final int STATE_STOPPED = 0;
    public static final int STATE_PLAYED = 1;
    public static final int STATE_BUFFERING = 2;
    public static final int STATE_PAUSED = 3;
    public static final int STATE_RESUME = 4;



    private int status;
    private SongModel songModel;

    public PlaybackEvent(int status) {
        this.status = status;
    }

    public PlaybackEvent(int status, SongModel songModel) {
        this.status = status;
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

}
