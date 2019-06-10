package com.siiberad.musicapp.model;


public class SongModel {

    private float ID;
    private String url;
    private String title;
    private String duration;
    private String album;
    private String singer;
    private String image;


    // Getter Methods

    public float getID() {
        return ID;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getAlbum() {
        return album;
    }

    public String getSinger() {
        return singer;
    }

    public String getImage() {
        return image;
    }

    // Setter Methods

    public void setID(float ID) {
        this.ID = ID;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public void setImage(String image) {
        this.image = image;
    }
}