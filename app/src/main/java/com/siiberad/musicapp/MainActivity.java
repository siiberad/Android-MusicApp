package com.siiberad.musicapp;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.siiberad.musicapp.adapter.SongPlaylistAdapter;
import com.siiberad.musicapp.event.PlaybackCommand;
import com.siiberad.musicapp.event.PlaybackEvent;
import com.siiberad.musicapp.event.PlaybackStatus;
import com.siiberad.musicapp.event.PlaybackStatusCheck;
import com.siiberad.musicapp.model.SongDao;
import com.siiberad.musicapp.model.SongModel;
import com.siiberad.musicapp.network.SongClientInstance;
import com.siiberad.musicapp.service.ForegroundService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private EventBus bus = EventBus.getDefault();

    ProgressDialog progressDialog;
    ImageButton playPause, nextButton, previousButton;
    SwipeRefreshLayout swipeLayout;
    ProgressBar spinner;

    boolean isPlaying=false;
    int index;

    TextView judul, penyanyi;

    RecyclerView recyclerView;
    SongPlaylistAdapter songPlaylistAdapter;

    private List<SongModel> songmodels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_main);

//        requestMultiplePermissions();

        /*GetSongList*/
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading....");
        progressDialog.show();
        getItem();


        playPause = (ImageButton) findViewById(R.id.play_button);
        playPause.setBackgroundResource(R.drawable.play_orange);
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPlaying) {
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PAUSE));
                }
                else {
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_RESUME));
                }
            }

        });

        nextButton = (ImageButton) findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Next Song", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_NEXT));
            }

        });

        previousButton = (ImageButton) findViewById(R.id.previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Previous Song", Toast.LENGTH_SHORT).show();
                EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PREVIOUS));
            }

        });

        judul = (TextView) findViewById(R.id.judul_play);
        penyanyi = (TextView) findViewById(R.id.penyanyi_play);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener((SwipeRefreshLayout.OnRefreshListener) this);

        spinner = (ProgressBar)findViewById(R.id.progress);
        Sprite doubleBounce = new DoubleBounce();
        spinner.setIndeterminateDrawable(doubleBounce);
    }


    //Foreground Service
    @SuppressLint("StaticFieldLeak")
    public void playSong(final int index, final List<SongModel> songModels) {

        Intent serviceIntent = new Intent(this, ForegroundService.class);
        if(!isMyServiceRunning(ForegroundService.class)) {
            ContextCompat.startForegroundService(this, serviceIntent);
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(1000);
                    EventBus.getDefault().post(new PlaybackCommand(PlaybackCommand.COMMAND_PLAY, index, songModels));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!bus.isRegistered(this)) {
            bus.register(this);
            EventBus.getDefault().post(new PlaybackStatusCheck());
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(bus.isRegistered(this)) {
            bus.unregister(this);
        }
    }

    @Subscribe
    public void OnEvent(final PlaybackEvent event){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(event!=null){
                    switch (event.getStatus()){
                        case PlaybackEvent.STATE_PLAYED:
                            isPlaying = true;
                            playPause.setVisibility(View.VISIBLE);
                            nextButton.setVisibility(View.VISIBLE);
                            previousButton.setVisibility(View.VISIBLE);
                            spinner.setVisibility(View.INVISIBLE);
                            playPause.setBackgroundResource(R.drawable.pause_oranges);
                            break;

                        case PlaybackEvent.STATE_BUFFERING:
                            judul.setText(event.getSongModel().getTitle());
                            penyanyi.setText(event.getSongModel().getSinger());
                            playPause.setVisibility(View.INVISIBLE);
                            nextButton.setVisibility(View.INVISIBLE);
                            previousButton.setVisibility(View.INVISIBLE);
                            spinner.setVisibility(View.VISIBLE);
                            break;

                        case PlaybackEvent.STATE_PAUSED:
                            isPlaying = false;
                            playPause.setBackgroundResource(R.drawable.play_orange);
                            break;

                        case PlaybackEvent.STATE_RESUME:
                            isPlaying = true;
                            playPause.setBackgroundResource(R.drawable.pause_oranges);
                            break;
                    }
                }
            }
        });

    }
    @Subscribe
    public void OnEvent(final PlaybackStatus event){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(event!=null){
                    switch (event.getStatus()){
                        case PlaybackStatus.STATUS_PLAY:
                            isPlaying = true;
                            judul.setText(event.getSongModel().getTitle());
                            penyanyi.setText(event.getSongModel().getSinger());
                            playPause.setBackgroundResource(R.drawable.pause_oranges);
                            break;

                        case PlaybackStatus.STATUS_PAUSE:
                            isPlaying = false;
                            judul.setText(event.getSongModel().getTitle());
                            penyanyi.setText(event.getSongModel().getSinger());
                            playPause.setBackgroundResource(R.drawable.play_orange);
                            break;
                    }
                }
            }
        });

    }


    //SwipeLayout Refresh
    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getItem();
                swipeLayout.setRefreshing(false);
            }
        }, 2000);
    }




    /*Set RecycleView Song List*/
    public void getItem() {

        SongDao service = SongClientInstance.getRetrofitInstance().create(SongDao.class);
        Call<List<SongModel>> call = service.getAllSong();

        call.enqueue(new Callback<List<SongModel>>() {
            @Override
            public void onResponse(Call<List<SongModel>> call, Response<List<SongModel>> response) {

                progressDialog.dismiss();
                generateDataList(response.body());
                songmodels = response.body();
            }

            @Override
            public void onFailure(Call<List<SongModel>> call, Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Something went wrong...Please try later!", Toast.LENGTH_SHORT).show();
                t.printStackTrace();
            }
        });
    }
    private void generateDataList(List<SongModel> photoList) {
        recyclerView = findViewById(R.id.customRecyclerView);
        Collections.sort(photoList, new Comparator<SongModel>() {
            @Override
            public int compare(SongModel o1, SongModel o2) {
                if(o1.getID() > o2.getID()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        songPlaylistAdapter = new SongPlaylistAdapter(this, photoList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(songPlaylistAdapter);

    }

    //OnClick RecycleView
    @Override
    public void onClick(View v) {
        float idTag = ((SongModel) v.getTag()).getID();

        for (int i = 0; i < songmodels.size(); i++) {
            if (songmodels.get(i).getID() == idTag) {
                index = i;
            }
        }
        Context context = v.getContext();
        Intent intent = new Intent(context, SongPlayerActivity.class);
        context.startActivity(intent);

        playSong(index, songmodels);
    }


    //Check if service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    /*Multi Permission*/
//    public void requestMultiplePermissions () {
//        Dexter.withActivity(this)
//                .withPermissions(
////                            Manifest.permission.CAMERA,
//                            Manifest.permission.INTERNET
////                            Manifest.permission.READ_EXTERNAL_STORAGE,
////                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
////                            Manifest.permission.ACCESS_COARSE_LOCATION,
////                            Manifest.permission.ACCESS_FINE_LOCATION
//                )
//                .withListener(new MultiplePermissionsListener() {
//                    @Override
//                    public void onPermissionsChecked(MultiplePermissionsReport report) {
//                        // check if all permissions are granted
//                        if (report.areAllPermissionsGranted()) {
//                            Toast.makeText(getApplicationContext(), "Welcome !", Toast.LENGTH_SHORT).show();
//                        }
//
//                        // check for permanent denial of any permission
//                        if (report.isAnyPermissionPermanentlyDenied()) {
//                            // show alert dialog navigating to Settings
//                            openSettingsDialog();
//                        }
//                    }
//                    @Override
//                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
//                        token.continuePermissionRequest();
//                    }
//                }).
//                withErrorListener(new PermissionRequestErrorListener() {
//                    @Override
//                    public void onError(DexterError error) {
//                        Toast.makeText(getApplicationContext(), "Some Error! ", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .onSameThread()
//                .check();
//    }
//    private void openSettingsDialog () {
//        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
//        builder.setTitle("Required Permissions");
//        builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
//        builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                Uri uri = Uri.fromParts("package", getPackageName(), null);
//                intent.setData(uri);
//                startActivityForResult(intent, 101);
//            }
//        });
//        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.cancel();
//            }
//        });
//        builder.show();
//    }
//    @Override
//    public void onRequestPermissionsResult ( int requestCode, @NonNull String[] permissions,
//                                             @NonNull int[] grantResults){
//        if (requestCode == STORAGE_PERMISSION_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//

}