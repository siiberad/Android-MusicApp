package com.siiberad.musicapp.model;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GetDataService {

    @GET("/photos")
    Call<List<SongModel>> getAllPhotos();
}
