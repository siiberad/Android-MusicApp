package com.siiberad.musicapp.event;

import com.siiberad.musicapp.model.SongModel;

public class PlaybackCommand {

    public static final int COMMAND_PLAY = 0;
    public static final int COMMAND_PAUSE = 1;
    public static final int COMMAND_RESUME = 2;

    int command;
    SongModel songModel;

    public PlaybackCommand(int command) {
        this.command = command;
    }

    public PlaybackCommand(int command, SongModel songModel) {
        this.command = command;
        this.songModel = songModel;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public SongModel getSongModel() {
        return songModel;
    }

    public void setSongModel(SongModel songModel) {
        this.songModel = songModel;
    }
}
