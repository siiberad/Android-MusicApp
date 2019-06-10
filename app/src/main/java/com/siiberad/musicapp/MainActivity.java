package com.siiberad.musicapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.siiberad.musicapp.adapter.SongPlaylistAdapter;
import com.siiberad.musicapp.model.SongDao;
import com.siiberad.musicapp.model.SongModel;
import com.siiberad.musicapp.network.SongClientInstance;

import java.io.IOException;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ProgressDialog progressDialog;
    private RecyclerView recyclerView;
    private SongPlaylistAdapter songPlaylistAdapter;
    private ImageButton playPause;
    MediaPlayer mPlayer;

    private final static int STORAGE_PERMISSION_CODE = 1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_main);

        requestMultiplePermissions();


        /*GetSongList*/
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();
        SongDao service = SongClientInstance.getRetrofitInstance().create(SongDao.class);
        Call<List<SongModel>> call = service.getAllSong();
        call.enqueue(new Callback<List<SongModel>>() {
            @Override
            public void onResponse(Call<List<SongModel>> call, Response<List<SongModel>> response) {
                progressDialog.dismiss();
                generateDataList(response.body());
            }

            @Override
            public void onFailure(Call<List<SongModel>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
            }
        });

        playPause = (ImageButton) findViewById(R.id.play_button);
        playPause.setBackgroundResource(R.drawable.play_orange);

        mPlayer = new MediaPlayer();
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(MainActivity.this,"Loading complete",Toast.LENGTH_LONG).show();
                mPlayer.start();
            }
        });

    }

    /*Set RecycleView Song List*/
    private void generateDataList(List<SongModel> photoList) {
        recyclerView = findViewById(R.id.customRecyclerView);
        songPlaylistAdapter = new SongPlaylistAdapter(this,photoList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(songPlaylistAdapter);
    }

    @Override
    public void onClick(View v) {

        if (mPlayer != null) {
            mPlayer.stop();mPlayer.reset();
            playPause.setBackgroundResource(R.drawable.progress_animation);
        }

        TextView judul, penyanyi;

        judul = (TextView) findViewById(R.id.judul_play);
        penyanyi = (TextView) findViewById(R.id.penyanyi_play);

        SongModel songModel = (SongModel) v.getTag();
        judul.setText(songModel.getTitle());
        penyanyi.setText(songModel.getSinger());


        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mPlayer.setDataSource(songModel.getUrl());
//            mPlaye.
            mPlayer.prepareAsync(); // might take long! (for buffering, etc)

            //Bikin info loading
            playPause.setBackgroundResource(R.drawable.ed_draw_animation_loading);


        } catch (IOException e) {
            e.printStackTrace();
        }

        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlayer.isPlaying()) {
                    playPause.setBackgroundResource(R.drawable.play_orange);
                    mPlayer.pause();
                } else {
                    playPause.setBackgroundResource(R.drawable.pause_oranges);
                    mPlayer.start();
                }
            }
        });
    }


        /*Multi Permission*/
    public void requestMultiplePermissions () {
        Dexter.withActivity(this)
                .withPermissions(
//                            Manifest.permission.CAMERA,
                            Manifest.permission.INTERNET
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.ACCESS_FINE_LOCATION
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "Welcome !", Toast.LENGTH_SHORT).show();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // show alert dialog navigating to Settings
                            openSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).
                withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }
    private void openSettingsDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Required Permissions");
        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, 101);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    @Override
    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
                                             @NonNull int[] grantResults){
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}