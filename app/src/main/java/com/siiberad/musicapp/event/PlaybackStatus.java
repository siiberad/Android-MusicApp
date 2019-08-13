package com.siiberad.musicapp.event;

import com.siiberad.musicapp.model.SongModel;

public class PlaybackStatus {

    public static final int STATUS_PLAY = 0;
    public static final int STATUS_PAUSE = 1;

    int status;
    int duration;
    SongModel songModel;

    public PlaybackStatus(int status) {
        this.status = status;
    }


    public PlaybackStatus(int status, SongModel songModel) {
        this.status = status;
        this.songModel = songModel;
    }

    public PlaybackStatus(int status, int duration, SongModel songModel) {
        this.status = status;
        this.duration = duration;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
