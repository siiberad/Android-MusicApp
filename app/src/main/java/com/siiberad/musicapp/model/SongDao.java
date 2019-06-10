package com.siiberad.musicapp.model;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface SongDao {

    @GET("/api/v1/playlist")
    Call<List<SongModel>> getAllSong();

}
