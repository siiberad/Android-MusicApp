package com.siiberad.musicapp.event;

import com.siiberad.musicapp.model.SongModel;

import java.util.ArrayList;
import java.util.List;

public class PlaybackCommand {

    public static final int COMMAND_PLAY = 0;
    public static final int COMMAND_PAUSE = 1;
    public static final int COMMAND_RESUME = 2;
    public static final int COMMAND_NEXT = 3;
    public static final int COMMAND_PREVIOUS = 4;
    public static final int COMMAND_SEEKBAR = 5;


    int command;
    SongModel songModel;
    int index;
    int remain_duration;
    List<SongModel> songModels;

    public PlaybackCommand(int command) {
        this.command = command;
    }

    public PlaybackCommand(int command, int index, List<SongModel> songModels) {
        this.command = command;
        this.index = index;
        this.songModels = songModels;
    }

    public PlaybackCommand(int command, int remain_duration, SongModel songModel) {
        this.command = command;
        this.remain_duration = remain_duration;
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

    public List<SongModel> getSongModels() {
        return songModels;
    }

    public void setSongModels(List<SongModel> songModels) {
        this.songModels = songModels;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getRemain_duration() {
        return remain_duration;
    }

    public void setRemain_duration(int remain_duration) {
        this.remain_duration = remain_duration;
    }
}
