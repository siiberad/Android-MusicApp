package com.siiberad.musicapp.event;

public class PlaybackProgressEvent {
    public int getCurrentPosition() {
        return currentPosition;
    }

    public PlaybackProgressEvent(int currentPosition, int totalDuration) {
        this.currentPosition = currentPosition;
        this.totalDuration = totalDuration;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getTotalDuration() {
        return totalDuration;
    }

    public void setTotalDuration(int totalDuration) {
        this.totalDuration = totalDuration;
    }

    int currentPosition, totalDuration;


}
